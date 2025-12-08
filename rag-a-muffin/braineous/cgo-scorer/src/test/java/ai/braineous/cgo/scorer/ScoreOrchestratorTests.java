package ai.braineous.cgo.scorer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.query.QueryTask;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScoreOrchestratorTests {
    @Test
    void orchestrate_withNullQueryExecution_shouldNotAddHistoryRecord() {
        // arrange
        Console.log("test_start", "orchestrate_withNullQueryExecution_shouldNotAddHistoryRecord");
        ScoreOrchestrator orchestrator = new ScoreOrchestrator();

        // act
        orchestrator.orchestrate(null);

        // assert
        HistoryStore store = orchestrator.getStore();
        int size = store.getAll().size();
        Console.log("history_size_after_null_orchestrate", size);

        assertEquals(0, size, "History should remain empty when queryExecution is null");
    }

    @Test
    void orchestrate_withValidExecution_shouldAppendHistoryRecord() {
        // arrange
        Console.log("test_start", "orchestrate_withValidExecution_shouldAppendHistoryRecord");
        ScoreOrchestrator orchestrator = new ScoreOrchestrator();

        int before = orchestrator.getStore().getAll().size();
        Console.log("history_size_before", before);

        QueryExecution<DummyTask> execution = createHappyPathExecution();

        // act
        orchestrator.orchestrate(execution);

        // assert
        HistoryStore store = orchestrator.getStore();
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
        // arrange
        Console.log("test_start", "orchestrate_whenCalledMultipleTimes_shouldAppendMultipleHistoryRecords");
        ScoreOrchestrator orchestrator = new ScoreOrchestrator();

        int before = orchestrator.getStore().getAll().size();
        Console.log("history_size_before", before);

        QueryExecution<DummyTask> execution1 = createHappyPathExecution();
        QueryExecution<DummyTask> execution2 = createHappyPathExecution();

        // act
        orchestrator.orchestrate(execution1);
        orchestrator.orchestrate(execution2);

        // assert
        int after = orchestrator.getStore().getAll().size();
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
        HistoryStore store = new HistoryStore();
        ScoreOrchestrator orchestrator = new ScoreOrchestrator(sequenceScorer, store);

        // act – 4 calls
        orchestrator.orchestrate(createHappyPathExecution());
        orchestrator.orchestrate(createHappyPathExecution());
        orchestrator.orchestrate(createHappyPathExecution());
        orchestrator.orchestrate(createHappyPathExecution());

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
}
