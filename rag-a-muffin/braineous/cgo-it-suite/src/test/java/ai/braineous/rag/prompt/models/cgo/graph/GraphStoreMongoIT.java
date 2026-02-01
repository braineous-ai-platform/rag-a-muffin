package ai.braineous.rag.prompt.models.cgo.graph;

// Integration test for GraphStoreMongo.
// Assumptions (you wire exact packages/imports):
// - MongoClient is available (QuarkusTest + test resource / Testcontainers / local mongo)
// - You have GraphStoreMongo, Fact, Edge, GraphSnapshot on classpath
// - Collections default to "cgo_nodes" and "cgo_edges" in db "cgo"

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class GraphStoreMongoIT {

    private MongoClient mongoClient;
    private GraphStoreMongo store;

    private static final String MONGO_URI = "mongodb://localhost:27017";

    @BeforeEach
    void setup() {
        mongoClient = com.mongodb.client.MongoClients.create(MONGO_URI);

        store = new GraphStoreMongo(mongoClient);

        mongoClient.getDatabase(GraphStoreMongo.DEFAULT_DB_NAME)
                .getCollection(GraphStoreMongo.DEFAULT_NODE_COLLECTION_NAME)
                .deleteMany(new Document());

        mongoClient.getDatabase(GraphStoreMongo.DEFAULT_DB_NAME)
                .getCollection(GraphStoreMongo.DEFAULT_EDGE_COLLECTION_NAME)
                .deleteMany(new Document());
    }

    @AfterEach
    void teardown() {
        if (mongoClient == null) {
            return;
        }

        try {
            mongoClient.getDatabase(GraphStoreMongo.DEFAULT_DB_NAME)
                    .getCollection(GraphStoreMongo.DEFAULT_NODE_COLLECTION_NAME)
                    .deleteMany(new Document());

            mongoClient.getDatabase(GraphStoreMongo.DEFAULT_DB_NAME)
                    .getCollection(GraphStoreMongo.DEFAULT_EDGE_COLLECTION_NAME)
                    .deleteMany(new Document());
        } finally {
            mongoClient.close();
        }
    }

    //--------------------------------------

    @Test
    void mutate_when_validFacts_persists_nodes_and_edge_and_snapshot_reads_all() {
        Fact aus = new Fact();
        aus.setId("Airport:AUS");
        aus.setText("{\"id\":\"Airport:AUS\",\"kind\":\"Airport\",\"name\":\"Austin\"}");
        aus.setMode("atomic");
        aus.setAttributes(new HashSet<String>());
        aus.addAttribute("airport");

        Fact dfw = new Fact();
        dfw.setId("Airport:DFW");
        dfw.setText("{\"id\":\"Airport:DFW\",\"kind\":\"Airport\",\"name\":\"Dallas\"}");
        dfw.setMode("atomic");
        dfw.setAttributes(new HashSet<String>());
        dfw.addAttribute("airport");

        Fact flightFact = new Fact();
        flightFact.setId("Flight:AUS-DFW:001");
        flightFact.setText("{\"id\":\"Flight:AUS-DFW:001\",\"kind\":\"Flight\",\"from\":\"Airport:AUS\",\"to\":\"Airport:DFW\"}");
        flightFact.setMode("relational");
        flightFact.setAttributes(new HashSet<String>());
        flightFact.addAttribute("flight");

        store.mutate(aus, dfw, flightFact);

        GraphSnapshot snap = store.snapshot();
        assertNotNull(snap);

        assertTrue(snap.nodes().containsKey("Airport:AUS"));
        assertTrue(snap.nodes().containsKey("Airport:DFW"));
        assertTrue(snap.edges().containsKey("Flight:AUS-DFW:001"));

        Fact aus2 = snap.nodes().get("Airport:AUS");
        assertEquals("Airport:AUS", aus2.getId());
        assertNotNull(aus2.getAttributes());
        assertTrue(aus2.getAttributes().contains("airport"));

        Edge e = snap.edges().get("Flight:AUS-DFW:001");
        assertEquals("Flight:AUS-DFW:001", e.getId());
        assertEquals("Airport:AUS", e.getFromFactId());
        assertEquals("Airport:DFW", e.getToFactId());
        assertEquals(1.0, e.getScore(), 0.00001);
        assertNotNull(e.getAttributes());
        assertTrue(e.getAttributes().contains("flight"));
    }

    @Test
    void upsertNode_when_existing_merges_attributes_and_overwrites_text_if_provided() {
        Fact f1 = new Fact();
        f1.setId("Airport:AUS");
        f1.setText("v1");
        f1.setMode("atomic");
        f1.setAttributes(new HashSet<String>());
        f1.addAttribute("a1");

        store.upsertNode(f1);

        Fact f2 = new Fact();
        f2.setId("Airport:AUS");
        f2.setText("v2"); // overwrite text
        f2.setMode("atomic");
        f2.setAttributes(new HashSet<String>());
        f2.addAttribute("a2"); // merge attributes

        store.upsertNode(f2);

        GraphSnapshot snap = store.snapshot();
        Fact got = snap.nodes().get("Airport:AUS");

        assertNotNull(got);
        assertEquals("Airport:AUS", got.getId());
        assertEquals("v2", got.getText());
        assertNotNull(got.getAttributes());
        assertTrue(got.getAttributes().contains("a1"));
        assertTrue(got.getAttributes().contains("a2"));
    }

    @Test
    void deleteNode_removes_node_and_related_edges() {
        Fact aus = new Fact();
        aus.setId("Airport:AUS");
        aus.setText("aus");
        aus.setMode("atomic");
        aus.setAttributes(new HashSet<String>());

        Fact dfw = new Fact();
        dfw.setId("Airport:DFW");
        dfw.setText("dfw");
        dfw.setMode("atomic");
        dfw.setAttributes(new HashSet<String>());

        Fact flightFact = new Fact();
        flightFact.setId("Flight:AUS-DFW:001");
        flightFact.setText("flight");
        flightFact.setMode("relational");
        flightFact.setAttributes(new HashSet<String>());

        store.mutate(aus, dfw, flightFact);

        // sanity
        GraphSnapshot snap1 = store.snapshot();
        assertTrue(snap1.nodes().containsKey("Airport:AUS"));
        assertTrue(snap1.edges().containsKey("Flight:AUS-DFW:001"));

        store.deleteNode(aus);

        GraphSnapshot snap2 = store.snapshot();
        assertFalse(snap2.nodes().containsKey("Airport:AUS"));
        // edge should be removed because fromFactId matches
        assertFalse(snap2.edges().containsKey("Flight:AUS-DFW:001"));
        // other node should remain
        assertTrue(snap2.nodes().containsKey("Airport:DFW"));
    }

    @Test
    void mutate_when_any_required_id_missing_is_noop() {
        Fact aus = new Fact();
        aus.setId("Airport:AUS");
        aus.setText("aus");
        aus.setMode("atomic");
        aus.setAttributes(new HashSet<String>());

        Fact dfw = new Fact();
        dfw.setId(null); // missing id
        dfw.setText("dfw");
        dfw.setMode("atomic");
        dfw.setAttributes(new HashSet<String>());

        Fact flightFact = new Fact();
        flightFact.setId("Flight:AUS-DFW:001");
        flightFact.setText("flight");
        flightFact.setMode("relational");
        flightFact.setAttributes(new HashSet<String>());

        store.mutate(aus, dfw, flightFact);

        GraphSnapshot snap = store.snapshot();
        // should be empty because mutate is noop if any id is missing
        assertTrue(snap.nodes().isEmpty());
        assertTrue(snap.edges().isEmpty());
    }
}

