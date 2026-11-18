package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
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
        Fact axis = new Fact(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}"
        );

        QueryExecution<ValidateTask> execution = pipeline.execute(
                request,
                axis,
                List.of(),
                null,
                "validate flight airports",
                null,
                null);

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

    @Test
    void execute_shouldKeepSevenArgumentQueryExecutionSignature() throws Exception {
        Method[] methods = QueryPipeline.class.getDeclaredMethods();
        int executeCount = 0;
        Method execute = null;
        int i = 0;
        while (i < methods.length) {
            if ("execute".equals(methods[i].getName())) {
                executeCount = executeCount + 1;
                execute = methods[i];
            }
            i = i + 1;
        }

        assertEquals(1, executeCount);
        assertNotNull(execute);
        Class<?>[] params = execute.getParameterTypes();
        assertEquals(7, params.length);
        assertEquals(QueryRequest.class, params[0]);
        assertEquals(Fact.class, params[1]);
        assertEquals(List.class, params[2]);
        assertEquals(String.class, params[3]);
        assertEquals(String.class, params[4]);
        assertEquals(String.class, params[5]);
        assertEquals(String.class, params[6]);
        assertEquals(QueryExecution.class, execute.getReturnType());
    }
}
