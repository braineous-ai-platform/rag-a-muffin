package ai.braineous.cgo.scorer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScoreOrchestratorTests {

    @BeforeEach
    public void setup(){
        HistoryStore store = HistoryStore.getInstance();
        store.clear();
    }

    @Test
    void orchestrate_withNullQueryExecution_shouldNotAddHistoryRecord() {
        HistoryStore store = HistoryStore.getInstance();

        // arrange
        Console.log("test_start", "orchestrate_withNullQueryExecution_shouldNotAddHistoryRecord");
        ScoreOrchestrator orchestrator = new ScoreOrchestrator();


        // act
        orchestrator.orchestrate(null);

        // assert
        int size = store.getAll().size();
        Console.log("history_size_after_null_orchestrate", size);

        assertEquals(0, size, "History should remain empty when queryExecution is null");
    }

    @Test
    void orchestrate_withValidExecution_shouldAppendHistoryRecord() {
        HistoryStore store = HistoryStore.getInstance();

        // arrange
        Console.log("test_start", "orchestrate_withValidExecution_shouldAppendHistoryRecord");
        ScoreOrchestrator orchestrator = new ScoreOrchestrator();
        orchestrator.setHistoryStore(store);

        int before = store.getAll().size();
        Console.log("history_size_before", before);

        QueryExecution<DummyTask> execution = createHappyPathExecution();

        // act
        orchestrator.orchestrate(execution);

        // assert
        int after = store.getAll().size();
        Console.log("history_size_after", after);

        assertEquals(before + 1, after, "History size should increase by 1");

        HistoryRecord lastRecord = store.getAll().get(after - 1);
        Console.log("last_history_record", lastRecord);

        assertNotNull(lastRecord.getResult(), "ScorerResult should not be null");
        assertEquals(1.0d, lastRecord.getResult().getScore(), 1e-9,
                "Happy-path execution should get score 1.0");
    }

    @Test
    void orchestrate_whenCalledMultipleTimes_shouldAppendMultipleHistoryRecords() {
        HistoryStore store = HistoryStore.getInstance();

        // arrange
        Console.log("test_start", "orchestrate_whenCalledMultipleTimes_shouldAppendMultipleHistoryRecords");
        ScoreOrchestrator orchestrator = new ScoreOrchestrator();
        orchestrator.setHistoryStore(store);

        int before = store.getAll().size();
        Console.log("history_size_before", before);

        QueryExecution<DummyTask> execution1 = createHappyPathExecution();
        QueryExecution<DummyTask> execution2 = createHappyPathExecution();

        // act
        orchestrator.orchestrate(execution1);
        orchestrator.orchestrate(execution2);

        // assert
        int after = store.getAll().size();
        Console.log("history_size_after", after);

        assertEquals(
                before + 2,
                after,
                "History size should increase by 2 when orchestrate is called twice with valid executions"
        );
    }

    @Test
    void orchestrate_withSequenceOfScores_shouldPersistScoresInOrder() {
        Console.log("test_start", "orchestrate_withSequenceOfScores_shouldPersistScoresInOrder");

        // expected score bands in order
        double[] expectedScores = {0.0, 0.33, 0.66, 1.0};

        // arrange
        SequenceScorer sequenceScorer = new SequenceScorer(expectedScores);
        ScoreOrchestrator orchestrator = new ScoreOrchestrator(sequenceScorer);
        orchestrator.setHistoryStore(HistoryStore.getInstance());

        // act – 4 calls
        orchestrator.orchestrate(createHappyPathExecution());
        orchestrator.orchestrate(createHappyPathExecution());
        orchestrator.orchestrate(createHappyPathExecution());
        orchestrator.orchestrate(createHappyPathExecution());

        HistoryStore store = HistoryStore.getInstance();
        // assert – history size + order + score values
        var records = store.getAll();
        int size = records.size();
        Console.log("history_size_after_sequence", size);

        assertEquals(4, size, "History should contain 4 records");

        for (int i = 0; i < expectedScores.length; i++) {
            double actual = records.get(i).getResult().getScore();
            Console.log("score_at_index_" + i, actual);
            assertEquals(expectedScores[i], actual, 1e-9,
                    "Score at index " + i + " should match expected band");
        }
    }

    @Test
    void execute_withConfiguredScorer_shouldAppendHistoryRecord() {
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta("v1", "validate_flight_airports", "desc");

        ValidateTask task = new ValidateTask(
                "validate flight airports",
                factId
        );

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);
        request.setAdapter(new FakeLlmAdapter());

        PromptBuilder promptBuilder =
                new PromptBuilder();

        // IMPORTANT: null LlmClient → pipeline.json used
        String raw = """
        {
          "result": {
            "ok": true,
            "code": "response.contract.ok",
            "message": "VALID",
            "stage": "llm_response_validation",
            "anchorId": null,
            "metadata": { "adapter": "fake" }
          }
        }
        """;
        CgoQueryPipeline pipeline =
                new CgoQueryPipeline(promptBuilder,
                        new FakeLlmClient(
                                raw)
                );


        // capture history size BEFORE
        HistoryStore store = HistoryStore.getInstance();
        int before = store.getAll().size();

        // act
        pipeline.setInMemoryMode(true);
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // assert pipeline basics
        assertNotNull(execution);
        assertNotNull(execution.getRawResponse());

        // assert REAL scorer side-effect
        int after = store.getAll().size();
        assertEquals(before + 1, after,
                "Real ScorerOrchestrator should append exactly one HistoryRecord");

        HistoryRecord last = store.getAll().get(after - 1);
        assertNotNull(last.getResult(), "ScorerResult must be present");
    }


    @Test
    void scorer_orchestrate_nullExecution_shouldNotAppendHistory() {
        Console.log("test_start", "scorer_orchestrate_nullExecution_shouldNotAppendHistory");

        HistoryStore store = HistoryStore.getInstance();
        store.clear();

        int before = store.getAll().size();
        Console.log("history_before", before);

        ScoreOrchestrator orchestrator = new ScoreOrchestrator();
        orchestrator.setHistoryStore(store);

        // act
        orchestrator.orchestrate(null);

        // assert
        int after = store.getAll().size();
        Console.log("history_after", after);

        assertEquals(before, after, "History should remain unchanged for null execution");
    }

    @Test
    void scorer_orchestrate_multipleExecutions_shouldAppendInOrder() {
        Console.log("test_start", "scorer_orchestrate_multipleExecutions_shouldAppendInOrder");

        HistoryStore store = HistoryStore.getInstance();
        store.clear();

        ScoreOrchestrator orchestrator = new ScoreOrchestrator();
        orchestrator.setHistoryStore(store);

        QueryExecution<DummyTask> e1 = createHappyPathExecution();
        QueryExecution<DummyTask> e2 = createHappyPathExecution();

        // act
        orchestrator.orchestrate(e1);
        orchestrator.orchestrate(e2);

        // assert
        var records = store.getAll();
        int size = records.size();
        Console.log("history_size", size);

        assertEquals(2, size, "Two executions should append two history records");

        HistoryRecord r1 = records.get(0);
        HistoryRecord r2 = records.get(1);

        Console.log("record_0_score", r1.getResult().getScore());
        Console.log("record_1_score", r2.getResult().getScore());

        assertNotNull(r1.getResult(), "First record should have result");
        assertNotNull(r2.getResult(), "Second record should have result");
    }

    @Test
    void scorer_orchestrate_shouldAlwaysPersistNonNullResult() {
        Console.log("test_start", "scorer_orchestrate_shouldAlwaysPersistNonNullResult");

        HistoryStore store = HistoryStore.getInstance();
        store.clear();

        ScoreOrchestrator orchestrator = new ScoreOrchestrator();
        orchestrator.setHistoryStore(store);

        // Build an execution with empty rawResponse (still recorded)
        Meta meta = new Meta("v1", "test_query_kind", "result must exist");
        GraphContext context = new GraphContext(Collections.emptyMap());
        DummyTask task = new DummyTask("dummy");

        QueryRequest<DummyTask> request = new QueryRequest<>(meta, context, task);

        ValidationResult ok = ValidationResult.ok("OK", "OK");
        QueryExecution<DummyTask> execution = new QueryExecution<>(request, "", ok, ok, ok);

        // act
        orchestrator.orchestrate(execution);

        // assert
        var records = store.getAll();
        int size = records.size();
        Console.log("history_size", size);

        assertEquals(1, size, "One orchestrate call should append one record");

        HistoryRecord last = records.get(0);
        Console.log("last_record", last);

        assertNotNull(last.getResult(), "ScorerResult must never be null");
        Console.log("score", last.getResult().getScore());
    }

    // ---- Helpers ---------------------------------------------------------

    private QueryExecution<DummyTask> createHappyPathExecution() {
        Meta meta = new Meta("v1", "test_query_kind", "test description");
        GraphContext context = new GraphContext(Collections.emptyMap());
        DummyTask task = new DummyTask("dummy task for scoring");

        QueryRequest<DummyTask> request = new QueryRequest<>(meta, context, task);

        ValidationResult ok = ValidationResult.ok("OK", "OK");

        return new QueryExecution<>(request, "raw-response", ok, ok, ok);
    }

    private static class DummyTask implements QueryTask {
        private final String description;

        DummyTask(String description) {
            this.description = description;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("description", this.description);
            return json;
        }
    }

    // ---- test double: returns a fixed sequence of scores -----------------

    private static class SequenceScorer extends Scorer {
        private final double[] scores;
        private int index = 0;

        private SequenceScorer(double[] scores) {
            this.scores = scores;
        }

        @Override
        public ScorerResult calculateScore(ScorerContext scorerContext) {
            double score;
            if (index < scores.length) {
                score = scores[index++];
            } else {
                // if called more than expected, just repeat last score
                score = scores[scores.length - 1];
            }
            Console.log("fake_scorer_called", "SequenceScorer returning " + score);

            // 🔧 IMPORTANT: tweak this to match your actual ScorerResult API.
            // Example options depending on your implementation:
            //   return new ScorerResult(score);
            //   return ScorerResult.ok(score, "test-sequence");
            //   return ScorerResult.of(score, Status.OK, "test-sequence");
            ScorerResult result = new ScorerResult();
            result.setScore(score);
            return result;
        }
    }

    private QueryExecution<DummyTask> createLlmResponseErrorExecution() {
        Meta meta = new Meta("v1", "test_query_kind", "test description");
        GraphContext context = new GraphContext(Collections.emptyMap());
        DummyTask task = new DummyTask("dummy task for scoring");
        QueryRequest<DummyTask> request = new QueryRequest<>(meta, context, task);

        ValidationResult ok = ValidationResult.ok("OK", "OK");
        ValidationResult err = ValidationResult.error(
                "response.contract.empty",
                "Raw LLM response is empty",
                "llm_response",
                null,
                Collections.singletonMap("rawResponse", "")
        );

        return new QueryExecution<>(request, "", ok, err, null);
    }

    private static class FakeLlmAdapter extends LlmAdapter{

        @Override
        public String invokeLlm(JsonObject prompt) {
            return "STUBBED";
        }
    }
}
