package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphStoreMongo implements GraphStore {

    public static final String DEFAULT_DB_NAME = "cgo";
    public static final String DEFAULT_NODE_COLLECTION_NAME = "cgo_nodes";
    public static final String DEFAULT_EDGE_COLLECTION_NAME = "cgo_edges";

    private static final String FIELD_FACT_ID = "factId";
    private static final String FIELD_EDGE_ID = "edgeId";

    private final MongoClient mongoClient;
    private final String dbName;
    private final String nodeCollectionName;
    private final String edgeCollectionName;

    public GraphStoreMongo(MongoClient mongoClient) {
        this(mongoClient, DEFAULT_DB_NAME, DEFAULT_NODE_COLLECTION_NAME, DEFAULT_EDGE_COLLECTION_NAME);
    }

    public GraphStoreMongo(MongoClient mongoClient, String dbName, String nodeCollectionName, String edgeCollectionName) {
        if (mongoClient == null) {
            throw new IllegalArgumentException("mongoClient cannot be null");
        }
        if (dbName == null || dbName.trim().isEmpty()) {
            throw new IllegalArgumentException("dbName cannot be null/empty");
        }
        if (nodeCollectionName == null || nodeCollectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("nodeCollectionName cannot be null/empty");
        }
        if (edgeCollectionName == null || edgeCollectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("edgeCollectionName cannot be null/empty");
        }

        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.nodeCollectionName = nodeCollectionName;
        this.edgeCollectionName = edgeCollectionName;

        ensureIndexes();
    }

    // ---------------------------------------------------------
    // GraphStore
    // ---------------------------------------------------------

    @Override
    public void upsertNode(Fact fact) {
        if (fact == null) {
            return;
        }

        String id = safe(fact.getId());
        if (id == null) {
            return;
        }

        MongoCollection<Document> col = getNodeCollection();

        // Read existing (to preserve merge semantics)
        Fact existing = findNode(id);

        Fact toPersist;
        if (existing == null) {
            toPersist = defensiveFactCopy(fact);
        } else {
            // UPDATE semantics: overwrite payload if provided
            if (fact.getText() != null) {
                existing.setText(fact.getText());
            }
            // always merge attributes
            mergeAttributes(existing, fact);
            toPersist = existing;
        }

        Document doc = Document.parse(toPersist.toJsonString());
        doc.put(FIELD_FACT_ID, id); // enforce axis

        col.replaceOne(
                Filters.eq(FIELD_FACT_ID, id),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public GraphSnapshot snapshot() {
        Map<String, Fact> nodes = new HashMap<String, Fact>();
        Map<String, Edge> edges = new HashMap<String, Edge>();

        MongoCollection<Document> nodeCol = getNodeCollection();
        for (Document doc : nodeCol.find()) {
            Fact f = parseFact(doc);
            if (f == null) {
                continue;
            }
            String id = safe(f.getId());
            if (id == null) {
                continue;
            }
            nodes.put(id, f);
        }

        MongoCollection<Document> edgeCol = getEdgeCollection();
        for (Document doc : edgeCol.find()) {
            Edge e = parseEdge(doc);
            if (e == null) {
                continue;
            }
            String id = safe(e.getId());
            if (id == null) {
                continue;
            }
            edges.put(id, e);
        }

        return new GraphSnapshot(
                java.util.Collections.unmodifiableMap(nodes),
                java.util.Collections.unmodifiableMap(edges)
        );
    }

    @Override
    public void deleteNode(Fact fact) {
        if (fact == null) {
            return;
        }

        String id = safe(fact.getId());
        if (id == null) {
            return;
        }

        // remove edges where this fact applies
        MongoCollection<Document> edgeCol = getEdgeCollection();
        edgeCol.deleteMany(
                Filters.or(
                        Filters.eq("fromFactId", id),
                        Filters.eq("toFactId", id)
                )
        );

        // remove node
        MongoCollection<Document> nodeCol = getNodeCollection();
        nodeCol.deleteOne(Filters.eq(FIELD_FACT_ID, id));
    }

    @Override
    public void mutate(Fact from, Fact to, Fact edgeFact) {
        if (from == null || safe(from.getId()) == null) return;
        if (to == null || safe(to.getId()) == null) return;
        if (edgeFact == null || safe(edgeFact.getId()) == null) return;

        this.upsertNode(from);
        this.upsertNode(to);
        this.upsertEdge(from, to, edgeFact);
    }

    // ---------------------------------------------------------
    // Internal: edges (same semantics as GraphStoreImpl)
    // ---------------------------------------------------------

    private void upsertEdge(Fact from, Fact to, Fact edgeFact) {
        String edgeId = safe(edgeFact.getId());
        if (edgeId == null) {
            return;
        }

        MongoCollection<Document> col = getEdgeCollection();

        Edge existing = findEdge(edgeId);

        Edge toPersist;
        if (existing == null) {
            Edge e = toEdge(from, to, edgeFact);
            if (e == null) {
                return;
            }
            toPersist = e;
        } else {
            // merge attributes & maybe score later; keep from/to stable
            mergeAttributes(existing, edgeFact);
            toPersist = existing;
        }

        Document doc = Document.parse(toPersist.toJsonString());
        doc.put(FIELD_EDGE_ID, edgeId); // enforce axis

        col.replaceOne(
                Filters.eq(FIELD_EDGE_ID, edgeId),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    private Edge toEdge(Fact from, Fact to, Fact edgeFact) {
        if (from == null || safe(from.getId()) == null) return null;
        if (to == null || safe(to.getId()) == null) return null;
        if (edgeFact == null || safe(edgeFact.getId()) == null) return null;

        Edge edge = new Edge();
        edge.setId(edgeFact.getId());
        edge.setText(edgeFact.getText());
        edge.setMode(edgeFact.getMode());

        // copy attributes defensively
        Set<String> attrs = edgeFact.getAttributes();
        if (attrs != null) {
            edge.setAttributes(new HashSet<String>(attrs));
        } else {
            edge.setAttributes(new HashSet<String>());
        }

        edge.setFromFactId(from.getId());
        edge.setToFactId(to.getId());

        // default score; tune later
        edge.setScore(1.0);

        return edge;
    }

    // ---------------------------------------------------------
    // Find + parse (CommitAuditViewMongoStore style)
    // ---------------------------------------------------------

    private Fact findNode(String factId) {
        Document doc = getNodeCollection().find(Filters.eq(FIELD_FACT_ID, factId)).first();
        return parseFact(doc);
    }

    private Edge findEdge(String edgeId) {
        Document doc = getEdgeCollection().find(Filters.eq(FIELD_EDGE_ID, edgeId)).first();
        return parseEdge(doc);
    }

    private Fact parseFact(Document doc) {
        if (doc == null) {
            return null;
        }
        try {
            JsonElement el = JsonParser.parseString(doc.toJson());
            if (el == null || !el.isJsonObject()) {
                return null;
            }
            Fact f = Fact.fromJson(el.getAsJsonObject());

            // enforce axis
            String axis = safe(doc.getString(FIELD_FACT_ID));
            if (axis != null && f != null) {
                f.setId(axis);
            }
            return f;
        } catch (RuntimeException re) {
            return null;
        }
    }

    private Edge parseEdge(Document doc) {
        if (doc == null) {
            return null;
        }
        try {
            JsonElement el = JsonParser.parseString(doc.toJson());
            if (el == null || !el.isJsonObject()) {
                return null;
            }
            Edge e = Edge.fromJson(el.getAsJsonObject());

            // enforce axis
            String axis = safe(doc.getString(FIELD_EDGE_ID));
            if (axis != null && e != null) {
                e.setId(axis);
            }
            return e;
        } catch (RuntimeException re) {
            return null;
        }
    }

    // ---------------------------------------------------------
    // Merge semantics (same as GraphStoreImpl)
    // ---------------------------------------------------------

    private void mergeAttributes(Fact target, Fact incoming) {
        if (target == null || incoming == null) return;
        if (incoming.getAttributes() == null) return;

        if (target.getAttributes() == null) {
            target.setAttributes(new HashSet<String>());
        }
        target.getAttributes().addAll(incoming.getAttributes());
    }

    private void mergeAttributes(Edge target, Fact incomingEdgeFact) {
        if (target == null || incomingEdgeFact == null) return;
        if (incomingEdgeFact.getAttributes() == null) return;

        if (target.getAttributes() == null) {
            target.setAttributes(new HashSet<String>());
        }
        target.getAttributes().addAll(incomingEdgeFact.getAttributes());
    }

    private Fact defensiveFactCopy(Fact fact) {
        Fact f = new Fact();
        f.setId(fact.getId());
        f.setText(fact.getText());
        f.setMode(fact.getMode());
        f.setValidationRule(fact.getValidationRule());

        if (fact.getAttributes() != null) {
            f.setAttributes(new HashSet<String>(fact.getAttributes()));
        } else {
            f.setAttributes(new HashSet<String>());
        }
        return f;
    }

    // ---------------------------------------------------------
    // Mongo plumbing + indexes
    // ---------------------------------------------------------

    private MongoCollection<Document> getNodeCollection() {
        MongoDatabase db = mongoClient.getDatabase(dbName);
        return db.getCollection(nodeCollectionName);
    }

    private MongoCollection<Document> getEdgeCollection() {
        MongoDatabase db = mongoClient.getDatabase(dbName);
        return db.getCollection(edgeCollectionName);
    }

    private void ensureIndexes() {
        try {
            getNodeCollection().createIndex(
                    new Document(FIELD_FACT_ID, 1),
                    new IndexOptions().unique(true).name("ux_factId")
            );

            getEdgeCollection().createIndex(
                    new Document(FIELD_EDGE_ID, 1),
                    new IndexOptions().unique(true).name("ux_edgeId")
            );

            // helps deleteNode sweep
            getEdgeCollection().createIndex(
                    new Document("fromFactId", 1),
                    new IndexOptions().unique(false).name("ix_fromFactId")
            );

            getEdgeCollection().createIndex(
                    new Document("toFactId", 1),
                    new IndexOptions().unique(false).name("ix_toFactId")
            );
        } catch (RuntimeException re) {
            // tolerate; IT will catch if truly broken
        }
    }

    private String safe(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }
}




