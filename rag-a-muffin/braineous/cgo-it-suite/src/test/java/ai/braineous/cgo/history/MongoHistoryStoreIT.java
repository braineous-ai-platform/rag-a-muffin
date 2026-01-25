package ai.braineous.cgo.history;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
public class MongoHistoryStoreIT {

    @Inject
    MongoClient mongoClient;




    @Test
    void addRecord_and_findHistory_and_getAll_roundtrip() {
        Console.log("IT", "MongoHistoryStoreIT.addRecord_and_findHistory_and_getAll_roundtrip");

        String dbName = "cgo_it";
        String collection = "history_roundtrip";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        HistoryRecord record = newRecord("TEST_QUERY");

        store.addRecord(record);

        HistoryView view = store.findHistory("TEST_QUERY");
        java.util.List<HistoryRecord> all = store.getAll();

        org.junit.jupiter.api.Assertions.assertNotNull(view);
        org.junit.jupiter.api.Assertions.assertEquals(1, view.size());
        org.junit.jupiter.api.Assertions.assertEquals(1, all.size());

        HistoryRecord fromView = view.getRecords().get(0);
        HistoryRecord fromAll = all.get(0);

        org.junit.jupiter.api.Assertions.assertEquals("TEST_QUERY", fromView.getQueryKind());
        org.junit.jupiter.api.Assertions.assertEquals("TEST_QUERY", fromAll.getQueryKind());
    }

    @Test
    void addRecord_nullRecord_isNoop() {
        Console.log("IT", "MongoHistoryStoreIT.addRecord_nullRecord_isNoop");

        String dbName = "cgo_it";
        String collection = "history_null_noop";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(null);

        java.util.List<HistoryRecord> all = store.getAll();
        org.junit.jupiter.api.Assertions.assertEquals(0, all.size());
    }

    @Test
    void findHistory_nullQueryKind_returnsEmpty() {
        Console.log("IT", "MongoHistoryStoreIT.findHistory_nullQueryKind_returnsEmpty");

        String dbName = "cgo_it";
        String collection = "history_find_null_querykind";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        // seed one legit record so we know "empty" is due to guard, not empty DB
        store.addRecord(newRecord("TEST_QUERY"));

        HistoryView view = store.findHistory(null);

        org.junit.jupiter.api.Assertions.assertNotNull(view);
        org.junit.jupiter.api.Assertions.assertEquals(0, view.size());
    }

    @Test
    void findHistory_blankQueryKind_returnsEmpty() {
        Console.log("IT", "MongoHistoryStoreIT.findHistory_blankQueryKind_returnsEmpty");

        String dbName = "cgo_it";
        String collection = "history_find_blank_querykind";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("TEST_QUERY"));

        HistoryView view = store.findHistory("   ");

        org.junit.jupiter.api.Assertions.assertNotNull(view);
        org.junit.jupiter.api.Assertions.assertEquals(0, view.size());
    }

    @Test
    void findHistory_trimmedQueryKind_findsRecord() {
        Console.log("IT", "MongoHistoryStoreIT.findHistory_trimmedQueryKind_findsRecord");

        String dbName = "cgo_it";
        String collection = "history_find_trimmed_querykind";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("TEST_QUERY"));

        HistoryView view = store.findHistory("  TEST_QUERY  ");

        org.junit.jupiter.api.Assertions.assertNotNull(view);
        org.junit.jupiter.api.Assertions.assertEquals(1, view.size());

        HistoryRecord r = view.getRecords().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("TEST_QUERY", r.getQueryKind());
    }

    @Test
    void addRecord_multipleSameQueryKind_accumulates() {
        Console.log("IT", "MongoHistoryStoreIT.addRecord_multipleSameQueryKind_accumulates");

        String dbName = "cgo_it";
        String collection = "history_multi_same_querykind";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("TEST_QUERY"));
        store.addRecord(newRecord("TEST_QUERY"));
        store.addRecord(newRecord("TEST_QUERY"));

        HistoryView view = store.findHistory("TEST_QUERY");
        java.util.List<HistoryRecord> all = store.getAll();

        org.junit.jupiter.api.Assertions.assertEquals(3, view.size());
        org.junit.jupiter.api.Assertions.assertEquals(3, all.size());
    }

    @Test
    void findHistory_mixedQueryKinds_isolatedResults() {
        Console.log("IT", "MongoHistoryStoreIT.findHistory_mixedQueryKinds_isolatedResults");

        String dbName = "cgo_it";
        String collection = "history_mixed_querykinds";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("A"));
        store.addRecord(newRecord("A"));
        store.addRecord(newRecord("B"));

        HistoryView aView = store.findHistory("A");
        HistoryView bView = store.findHistory("B");
        java.util.List<HistoryRecord> all = store.getAll();

        org.junit.jupiter.api.Assertions.assertEquals(2, aView.size());
        org.junit.jupiter.api.Assertions.assertEquals(1, bView.size());
        org.junit.jupiter.api.Assertions.assertEquals(3, all.size());
    }

    @Test
    void addRecord_nullQueryKind_isNotFindableButIsInGetAll() {
        Console.log("IT", "MongoHistoryStoreIT.addRecord_nullQueryKind_isNotFindableButIsInGetAll");

        String dbName = "cgo_it";
        String collection = "history_null_querykind_behavior";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        // record with null queryKind
        HistoryRecord record = newRecord(null);
        store.addRecord(record);

        HistoryView view = store.findHistory("TEST_QUERY");
        java.util.List<HistoryRecord> all = store.getAll();

        org.junit.jupiter.api.Assertions.assertEquals(0, view.size());
        org.junit.jupiter.api.Assertions.assertEquals(1, all.size());

        HistoryRecord only = all.get(0);
        org.junit.jupiter.api.Assertions.assertEquals(null, only.getQueryKind());
    }

    @Test
    void ctor_invalidArgs_failFast() {
        Console.log("IT", "MongoHistoryStoreIT.ctor_invalidArgs_failFast");

        // null client
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        new MongoHistoryStore(null, "db", "col");
                    }
                }
        );

        // blank dbName
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        new MongoHistoryStore(mongoClient, "   ", "col");
                    }
                }
        );

        // blank collectionName
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        new MongoHistoryStore(mongoClient, "db", "   ");
                    }
                }
        );
    }

    @Test
    void clear_emptiesCollection() {
        Console.log("IT", "MongoHistoryStoreIT.clear_emptiesCollection");

        String dbName = "cgo_it";
        String collection = "history_clear_empties";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        store.addRecord(newRecord("A"));
        store.addRecord(newRecord("B"));

        org.junit.jupiter.api.Assertions.assertEquals(2, store.getAll().size());

        store.clear();

        org.junit.jupiter.api.Assertions.assertEquals(0, store.getAll().size());
    }

    @Test
    void roundtrip_preservesValidateTaskFactId() {
        Console.log("IT", "MongoHistoryStoreIT.roundtrip_preservesValidateTaskFactId");

        String dbName = "cgo_it";
        String collection = "history_roundtrip_preserve_task";

        MongoHistoryStore store = new MongoHistoryStore(mongoClient, dbName, collection);
        store.clear();

        // build a record with a specific factId
        Meta meta = new Meta("v1", "TEST_QUERY", "IT");
        GraphContext ctx = new GraphContext(java.util.Map.of());
        ValidateTask task = new ValidateTask("IT task", "Flight:F999");
        QueryRequest<ValidateTask> req = new QueryRequest<ValidateTask>(meta, ctx, task);
        QueryExecution<ValidateTask> exec = new QueryExecution<ValidateTask>(req);
        HistoryRecord record = new HistoryRecord(exec, null);

        store.addRecord(record);

        HistoryView view = store.findHistory("TEST_QUERY");
        org.junit.jupiter.api.Assertions.assertEquals(1, view.size());

        HistoryRecord fromView = view.getRecords().get(0);

        // pull the task back out
        QueryExecution<?> qe = fromView.getQueryExecution();
        QueryRequest<?> rq = qe.getRequest();
        QueryTask t = rq.getTask();

        org.junit.jupiter.api.Assertions.assertEquals(ValidateTask.class.getName(), t.getClass().getName());
        ValidateTask vt = (ValidateTask) t;
        org.junit.jupiter.api.Assertions.assertEquals("Flight:F999", vt.getFactId());
    }



    ///---------------------
    private HistoryRecord newRecord(String queryKind) {

        Meta meta = new Meta("v1", queryKind, "IT");

        GraphContext ctx = new GraphContext(java.util.Map.of());

        ValidateTask task = new ValidateTask("IT task", "Flight:F100");

        QueryRequest<ValidateTask> req = new QueryRequest<ValidateTask>(meta, ctx, task);

        QueryExecution<ValidateTask> exec = new QueryExecution<ValidateTask>(req);

        return new HistoryRecord(exec, null);
    }


}

