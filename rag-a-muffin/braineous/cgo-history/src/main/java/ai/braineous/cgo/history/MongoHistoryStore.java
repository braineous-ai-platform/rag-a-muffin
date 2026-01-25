package ai.braineous.cgo.history;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoHistoryStore implements Store {

    private final MongoClient mongoClient;
    private final String dbName;
    private final String collectionName;

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
}
