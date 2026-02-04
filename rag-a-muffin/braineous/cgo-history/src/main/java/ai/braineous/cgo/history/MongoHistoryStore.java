package ai.braineous.cgo.history;

import com.google.gson.JsonObject;
import com.mongodb.ClientSessionOptions;
import com.mongodb.client.*;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.connection.ClusterDescription;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.MongoClients;

public class MongoHistoryStore implements Store {

    public static final String DEFAULT_DB_NAME = "cgo";
    public static final String DEFAULT_COLLECTION_NAME = "history_store";

    private MongoClient mongoClient;
    private String dbName;
    private String collectionName;

    public MongoHistoryStore() {
        this.dbName = DEFAULT_DB_NAME;
        this.collectionName = DEFAULT_COLLECTION_NAME;

        // TEMP hardcode for IT/local. Refactor back into pipeline.json later.
        String connectionString = "mongodb://localhost:27017";

        this.mongoClient = MongoClients.create(connectionString);
    }

    public MongoHistoryStore(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.dbName = DEFAULT_DB_NAME;
        this.collectionName = DEFAULT_COLLECTION_NAME;
    }


    public MongoHistoryStore(MongoClient mongoClient, String dbName, String collectionName) {
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

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void addRecord(HistoryRecord record) {
        if (record == null) {
            return;
        }

        MongoCollection<Document> col = getCollection();

        String queryKind = safeQueryKind(record);

        JsonObject recordJson = record.toJson();
        Document doc = new Document();
        doc.put("queryKind", queryKind);
        doc.put("record", Document.parse(recordJson.toString()));

        col.insertOne(doc);
    }

    @Override
    public HistoryView findHistory(String queryKind) {

        HistoryView view = new HistoryView();

        if (queryKind == null) {
            return view;
        }

        String needle = queryKind.trim();
        if (needle.isEmpty()) {
            return view;
        }

        MongoCollection<Document> col = getCollection();

        for (Document doc : col.find(eq("queryKind", needle))) {
            Document recordDoc = (Document) doc.get("record");
            if (recordDoc != null) {
                JsonObject recordJson = com.google.gson.JsonParser.parseString(recordDoc.toJson()).getAsJsonObject();
                HistoryRecord r = HistoryRecord.fromJson(recordJson);
                view.addRecord(r);
            }
        }

        return view;
    }

    @Override
    public List<HistoryRecord> getAll() {

        List<HistoryRecord> out = new ArrayList<>();

        MongoCollection<Document> col = getCollection();

        for (Document doc : col.find()) {
            Document recordDoc = (Document) doc.get("record");
            if (recordDoc != null) {
                JsonObject recordJson = com.google.gson.JsonParser.parseString(recordDoc.toJson()).getAsJsonObject();
                HistoryRecord r = HistoryRecord.fromJson(recordJson);
                out.add(r);
            }
        }

        return out;
    }


    public void clear() {
        getCollection().deleteMany(new Document());
    }

    // ---------------------------------------------------------

    private MongoCollection<Document> getCollection() {
        MongoDatabase db = mongoClient.getDatabase(dbName);
        return db.getCollection(collectionName);
    }

    private String safeQueryKind(HistoryRecord record) {
        try {
            String qk = record.getQueryKind();
            if (qk == null) {
                return null;
            }
            String trimmed = qk.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return trimmed;
        } catch (RuntimeException e) {
            // if request/meta isn't present, don't explode v1
            return null;
        }
    }

    ///----------------------
    @Override
    public void upsertPending(HistoryRecord record) {

        if (record == null) {
            return;
        }

        MongoCollection<Document> col = getCollection();

        JsonObject recordJson = record.toJson();

        String factId = null;

        // ---- extract factId ----
        try {
            if (recordJson.has("queryExecution") && !recordJson.get("queryExecution").isJsonNull()) {
                JsonObject qe = recordJson.getAsJsonObject("queryExecution");

                if (qe.has("request") && !qe.get("request").isJsonNull()) {
                    JsonObject req = qe.getAsJsonObject("request");

                    // path: queryExecution.request.factId
                    if (factId == null && req.has("factId") && !req.get("factId").isJsonNull()) {
                        factId = req.get("factId").getAsString();
                    }

                    // path: queryExecution.request.task.factId / id
                    if (factId == null && req.has("task") && !req.get("task").isJsonNull()) {
                        JsonObject task = req.getAsJsonObject("task");
                        if (task.has("factId") && !task.get("factId").isJsonNull()) {
                            factId = task.get("factId").getAsString();
                        }
                        if (factId == null && task.has("id") && !task.get("id").isJsonNull()) {
                            factId = task.get("id").getAsString();
                        }
                    }

                    // path: queryExecution.request.anchor (last resort)
                    if (factId == null && req.has("anchor") && !req.get("anchor").isJsonNull()) {
                        factId = req.get("anchor").getAsString();
                    }
                }
            }
        } catch (RuntimeException e) {
            return;
        }

        if (factId == null || factId.trim().isEmpty()) {
            return;
        }

        String fid = factId.trim();

        String queryKind = safeQueryKind(record);

        Document recordDoc = Document.parse(recordJson.toString());

        Document setDoc = new Document();
        if (queryKind != null) {
            setDoc.put("queryKind", queryKind);
        }
        setDoc.put("record", recordDoc);

        Document update = new Document("$set", setDoc);

        // --- filter: match factId across likely nested paths ---
        Document filter = new Document("$or", Arrays.asList(
                new Document("record.queryExecution.request.factId", fid),
                new Document("record.queryExecution.request.task.factId", fid),
                new Document("record.queryExecution.request.task.id", fid),
                new Document("record.queryExecution.request.anchor", fid)
        ));

        UpdateOptions opts = new UpdateOptions();
        opts.upsert(true);

        col.updateOne(filter, update, opts);
    }

    @Override
    public void markAccepted(String factId, String executionId, String commitId) {

        if (factId == null || factId.trim().isEmpty()) {
            return;
        }
        if (commitId == null || commitId.trim().isEmpty()) {
            return;
        }

        MongoCollection<Document> col = getCollection();

        String fid = factId.trim();
        String cid = commitId.trim();

        String now = Instant.now().toString();

        Document filter = new Document("$or", Arrays.asList(
                new Document("record.queryExecution.request.factId", fid),
                new Document("record.queryExecution.request.task.factId", fid),
                new Document("record.queryExecution.request.task.id", fid),
                new Document("record.queryExecution.request.anchor", fid)
        ));

        Document set = new Document();
        set.put("record.status", "ACCEPTED");
        set.put("record.approvedCommitId", cid);
        set.put("record.updatedAt", now);

        Document update = new Document("$set", set);

        col.updateOne(filter, update);
    }

    @Override
    public List<HistoryRecord> findByStatus(String factId, HistoryStatus status) {

        List<HistoryRecord> out = new ArrayList<>();

        if (factId == null) {
            return out;
        }
        String fid = factId.trim();
        if (fid.isEmpty()) {
            return out;
        }
        if (status == null) {
            return out;
        }

        MongoCollection<Document> col = getCollection();

        Document factOr = new Document("$or", Arrays.asList(
                new Document("record.queryExecution.request.factId", fid),
                new Document("record.queryExecution.request.task.factId", fid),
                new Document("record.queryExecution.request.task.id", fid),
                new Document("record.queryExecution.request.anchor", fid)
        ));

        Document statusEq = new Document("record.status", status.name());

        Document filter = new Document("$and", Arrays.asList(factOr, statusEq));

        FindIterable<Document> it =
                col.find(filter)
                        .sort(new Document("record.updatedAt", -1).append("record.createdAt", -1));

        for (Document doc : it) {
            Document recordDoc = (Document) doc.get("record");
            if (recordDoc != null) {
                JsonObject recordJson =
                        com.google.gson.JsonParser.parseString(recordDoc.toJson()).getAsJsonObject();
                HistoryRecord r = HistoryRecord.fromJson(recordJson);
                out.add(r);
            }
        }

        return out;
    }

    @Override
    public HistoryRecord findLatest(String factId) {

        if (factId == null) {
            return null;
        }
        String fid = factId.trim();
        if (fid.isEmpty()) {
            return null;
        }

        MongoCollection<Document> col = getCollection();

        Document factOr = new Document("$or", Arrays.asList(
                new Document("record.queryExecution.request.factId", fid),
                new Document("record.queryExecution.request.task.factId", fid),
                new Document("record.queryExecution.request.task.id", fid),
                new Document("record.queryExecution.request.anchor", fid)
        ));

        Document doc =
                col.find(factOr)
                        .sort(new Document("record.updatedAt", -1).append("record.createdAt", -1))
                        .limit(1)
                        .first();

        if (doc == null) {
            return null;
        }

        Document recordDoc = (Document) doc.get("record");
        if (recordDoc == null) {
            return null;
        }

        JsonObject recordJson =
                com.google.gson.JsonParser.parseString(recordDoc.toJson()).getAsJsonObject();

        return HistoryRecord.fromJson(recordJson);
    }

}
