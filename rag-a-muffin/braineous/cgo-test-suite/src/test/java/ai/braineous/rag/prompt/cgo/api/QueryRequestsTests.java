package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QueryRequestsTests {
    @Test
    void validateTask_shouldBuildQueryRequestWithGivenMetaContextAndTask() {
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight has valid departure and arrival airport codes using graph context."
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

        // act
        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        // assert
        assertNotNull(request, "QueryRequest should not be null");

        // meta should be the same instance we passed in
        assertSame(meta, request.getMeta(), "Meta should be preserved as-is");

        // context should be the same instance we passed in
        assertSame(context, request.getContext(), "GraphContext should be preserved as-is");
        assertEquals(1, request.getContext().getNodes().size(), "Context should contain one node");
        assertTrue(request.getContext().getNodes().containsKey(factId),
                "Context should contain the node for the given factId");

        // task should be the same instance we passed in
        assertSame(task, request.getTask(), "Task should be preserved as-is");
        assertEquals(factId, request.getTask().getFactId(), "Task should keep the correct factId");
        assertNotNull(request.getTask().getDescription(), "Task description should not be null");
        assertFalse(request.getTask().getDescription().isBlank(), "Task description should not be blank");
    }
}
