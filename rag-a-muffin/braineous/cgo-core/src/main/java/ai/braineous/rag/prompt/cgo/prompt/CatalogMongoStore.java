package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

public class CatalogMongoStore {

    public static final String DEFAULT_DB_NAME = "cgo";
    public static final String DEFAULT_COLLECTION_NAME = "catalog_entries";

    private MongoClient mongoClient;
    private String dbName;
    private String collectionName;

    public CatalogMongoStore(MongoClient mongoClient) {
        this(mongoClient, DEFAULT_DB_NAME, DEFAULT_COLLECTION_NAME);
    }

    public CatalogMongoStore(MongoClient mongoClient, String dbName, String collectionName) {
        if (mongoClient == null) {
            throw new IllegalArgumentException("mongoClient cannot be null");
        }
        if (dbName == null || dbName.trim().isEmpty()) {
            throw new IllegalArgumentException("dbName cannot be null/empty");
        }
        if (collectionName == null || collectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("collectionName cannot be null/empty");
        }
        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    // -------------------------
    // Orchestrator-facing ops
    // -------------------------

    public CatalogEntry getEntry(String queryKind) {
        String needle = safe(queryKind);
        if (needle == null) {
            return null;
        }

        Document doc = getCollection().find(eq("queryKind", needle)).first();
        if (doc == null) {
            return null;
        }

        Object raw = doc.get("entry");
        if (!(raw instanceof Document)) {
            return null;
        }

        Document entryDoc = (Document) raw;

        try {
            JsonElement el = JsonParser.parseString(entryDoc.toJson());
            if (el == null || !el.isJsonObject()) {
                return null;
            }
            return CatalogEntry.fromJson(el.getAsJsonObject());
        } catch (RuntimeException re) {
            return null;
        }
    }

    public void upsertEntry(CatalogEntry entry) {
        if (entry == null) {
            return;
        }

        String qk = entry.safeQueryKind();
        if (qk == null) {
            return;
        }

        JsonObject entryJson = entry.toJson();

        Document doc = new Document();
        doc.put("queryKind", qk);
        doc.put("entry", Document.parse(entryJson.toString()));

        getCollection().replaceOne(eq("queryKind", qk), doc, new ReplaceOptions().upsert(true));
    }

    public List<CatalogEntry> listEntries() {
        List<CatalogEntry> out = new ArrayList<CatalogEntry>();

        for (Document doc : getCollection().find()) {

            Object raw = doc.get("entry");
            if (!(raw instanceof Document)) {
                continue;
            }

            Document entryDoc = (Document) raw;

            try {
                JsonElement el = JsonParser.parseString(entryDoc.toJson());
                if (el == null || !el.isJsonObject()) {
                    continue;
                }

                CatalogEntry e = CatalogEntry.fromJson(el.getAsJsonObject());
                if (e != null) {
                    out.add(e);
                }
            } catch (RuntimeException re) {
                // skip bad row
            }
        }

        return out;
    }

    public CatalogSnapshot resolveSnapshot(String queryKind) {
        // Currently ignores queryKind filtering; snapshot is "all entries".
        // Orchestrator can still call getEntry(queryKind) for a specific one.
        CatalogSnapshot snap = new CatalogSnapshot();

        Map<String, CatalogEntry> map = new HashMap<String, CatalogEntry>();

        List<CatalogEntry> all = listEntries();
        for (int i = 0; i < all.size(); i++) {
            CatalogEntry e = all.get(i);
            if (e == null) {
                continue;
            }
            String k = e.safeQueryKind();
            if (k == null) {
                continue;
            }
            map.put(k, e);
        }

        snap.setByQueryKind(map);
        return snap;
    }

    public void clear() {
        getCollection().deleteMany(new Document());
    }

    // -------------------------
    // helpers
    // -------------------------

    private MongoCollection<Document> getCollection() {
        MongoDatabase db = mongoClient.getDatabase(dbName);
        return db.getCollection(collectionName);
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


