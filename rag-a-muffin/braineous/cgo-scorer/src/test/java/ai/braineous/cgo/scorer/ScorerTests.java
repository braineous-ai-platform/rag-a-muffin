package ai.braineous.cgo.scorer;

import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScorerTests {
    @Test
    void calculateScore_withValidQueryExecution_shouldReturnNonNullResult() {
        // arrange
        Meta meta = new Meta("v1", "validate_flight_airports", "test description");

        QueryRequest request = mock(QueryRequest.class);
        when(request.getMeta()).thenReturn(meta);

        QueryExecution execution = mock(QueryExecution.class);
        when(execution.getRequest()).thenReturn(request);

        ScorerContext context = new ScorerContext(execution);
        Scorer scorer = new Scorer();

        // ---- LOG: PHASE 1 ----
        Console.log("QUERY_EXECUTION", execution);

        // act
        ScorerResult result = scorer.calculateScore(context);

        // ---- LOG: PHASE 2 ----
        Console.log("SCORER_RESULT", result);

        // assert
        assertNotNull(result, "ScorerResult must not be null");
        assertNotNull(result.getStatus(), "ScorerResult status must not be null");

        assertEquals(
                ScorerResult.Status.UNKNOWN,
                result.getStatus(),
                "Default status should be UNKNOWN"
        );
    }
}
