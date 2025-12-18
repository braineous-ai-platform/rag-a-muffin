package ai.braineous.cgo.observer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.HistoryView;
import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.prompt.LlmClient;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.SimpleResponseContractRegistry;
import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTests {
    @BeforeEach
    public void setup(){
        HistoryStore store = HistoryStore.getInstance();
        store.clear();
    }

    @Test
    void snapshotForQueryKind_nullOrBlank_returnsEmpty() {
        Console.log("test_start", "snapshotForQueryKind_nullOrBlank_returnsEmpty");

        HistoryStore store = HistoryStore.getInstance();
        store.clear();

        Observer observer = new Observer();

        WhySnapshot s1 = observer.snapshotForQueryKind(null);
        Console.log("snapshot_null", s1);
        assertEquals(WhySnapshot.empty(), s1);

        WhySnapshot s2 = observer.snapshotForQueryKind("   ");
        Console.log("snapshot_blank", s2);
        assertEquals(WhySnapshot.empty(), s2);
    }

    @Test
    void snapshotForQueryKind_pipelineExecute_thenSnapshotReadsHistory() {
        Console.log("test_start", "snapshotForQueryKind_pipelineExecute_thenSnapshotReadsHistory");

        HistoryStore store = HistoryStore.getInstance();
        store.clear();

        int before = store.getAll().size();
        Console.log("history_before", before);

        String factId = "Flight:F300";
        String queryKind = "validate_flight_airports"; // <-- use the real one you want

        Meta meta = new Meta("v1", queryKind, "spine");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F300\",\"kind\":\"Flight\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));
        QueryRequest<ValidateTask> request = QueryRequests.validateTask(meta, task, context, factId);

        request.setAdapter(new LlmAdapter() {
            @Override
            public String invokeLlm(JsonObject prompt) {
                Console.log("fake_adapter_invoked", prompt == null ? "prompt=null" : "prompt=ok");
                return "{\"result\":{\"status\":\"VALID\"}}";
            }
        });

        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());
        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, (LlmClient) null);

        // act
        QueryExecution<ValidateTask> exec = pipeline.execute(request);
        assertNotNull(exec, "execution should not be null");

        // assert: history appended
        int after = store.getAll().size();
        Console.log("history_after", after);
        assertEquals(before + 1, after, "pipeline/scorer must append exactly one history record");

        HistoryRecord last = store.getAll().get(after - 1);
        Console.log("last_record", last);
        assertNotNull(last.getResult(), "ScorerResult should not be null");

        // assert: observer snapshot sees that history for this queryKind
        Observer observer = new Observer();

        WhySnapshot snapshot = observer.snapshotForQueryKind(queryKind);
        Console.log("observer_snapshot", snapshot);

        assertNotNull(snapshot);
        assertTrue(snapshot.getTotalEvents() >= 1, "snapshot should see at least 1 event for queryKind");
        assertNotNull(snapshot.getLastScore(), "lastScore should exist when scorer appended a score");
    }

    @Test
    void snapshotForQueryKind_noMatchingRecords_returnsEmpty() {
        Console.log("test_start", "snapshotForQueryKind_noMatchingRecords_returnsEmpty");

        HistoryStore store = HistoryStore.getInstance();
        store.clear();

        int before = store.getAll().size();
        Console.log("history_before", before);

        // run pipeline once with SOME queryKind
        String factId = "Flight:F400";
        String existingQueryKind = "validate_flight_airports";
        String missingQueryKind = "some_other_query_kind";

        Meta meta = new Meta("v1", existingQueryKind, "spine");
        ValidateTask task = new ValidateTask("validate flight airports", factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F400\",\"kind\":\"Flight\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));
        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        request.setAdapter(new LlmAdapter() {
            @Override
            public String invokeLlm(JsonObject prompt) {
                Console.log("fake_adapter_invoked", "ok");
                return "{\"result\":{\"status\":\"VALID\"}}";
            }
        });

        PromptBuilder promptBuilder =
                new PromptBuilder(new SimpleResponseContractRegistry());
        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(promptBuilder, (LlmClient) null);

        pipeline.execute(request);

        int after = store.getAll().size();
        Console.log("history_after", after);
        assertEquals(before + 1, after);

        // now ask observer for a DIFFERENT queryKind
        Observer observer = new Observer();
        WhySnapshot snapshot =
                observer.snapshotForQueryKind(missingQueryKind);

        Console.log("observer_snapshot", snapshot);

        assertEquals(WhySnapshot.empty(), snapshot);
    }
}