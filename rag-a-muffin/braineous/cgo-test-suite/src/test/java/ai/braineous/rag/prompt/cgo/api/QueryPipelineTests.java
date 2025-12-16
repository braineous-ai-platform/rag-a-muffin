package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryPipelineTests {

    @Test
    void execute_shouldPassThroughRequestAndWrapInQueryExecution() {
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight fact has valid departure and arrival airport codes using graph context."
        );

        ValidateTask task = new ValidateTask(
                "Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. " +
                        "A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.",
                factId
        );

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(
                Map.of(factId, node)
        );

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        FakeQueryPipeline pipeline = new FakeQueryPipeline();

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // assert
        assertNotNull(execution, "QueryExecution should not be null");
        assertNotNull(execution.getRequest(), "Execution should contain the original request");

        // Same instance should be passed through
        assertSame(request, execution.getRequest(), "Execution should wrap the exact same QueryRequest instance");
        assertSame(request, pipeline.getLastRequest(), "Pipeline should capture the last request");

        // And the request internals should be intact
        assertSame(meta, execution.getRequest().getMeta(), "Meta should be preserved");
        assertSame(context, execution.getRequest().getContext(), "Context should be preserved");
        assertSame(task, execution.getRequest().getTask(), "Task should be preserved");
        assertEquals(factId, execution.getRequest().getTask().getFactId(), "Task should still reference the same factId");
    }
}
