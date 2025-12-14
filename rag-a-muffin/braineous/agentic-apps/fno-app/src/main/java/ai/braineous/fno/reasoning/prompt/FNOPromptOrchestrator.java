package ai.braineous.fno.reasoning.prompt;

import ai.braineous.rag.prompt.cgo.api.*;

import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.SimpleResponseContractRegistry;

import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;

import ai.braineous.rag.prompt.observe.Console;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public class FNOPromptOrchestrator {

    public void orchestrate(JsonObject jsonObject){
        // arrange
        String factId = "Flight:F100";

        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate that the selected flight fact has valid departure and arrival airport codes using graph context."
        );

        String taskDescription =
                "Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. " +
                        "A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.";

        ValidateTask task = new ValidateTask(taskDescription, factId);

        Node node = new Node(
                factId,
                "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                List.of(),
                Node.Mode.RELATIONAL
        );

        GraphContext context = new GraphContext(Map.of(factId, node));

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        // PromptBuilder with NO prompt validator
        PromptBuilder promptBuilder = new PromptBuilder(new SimpleResponseContractRegistry());

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder, null);

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // console inspect
        Console.log("happy.rawResponse", execution.getRawResponse());
        Console.log("happy.promptValidation", execution.getPromptValidation());
        Console.log("happy.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("happy.domainValidation", execution.getDomainValidation());
    }
}
