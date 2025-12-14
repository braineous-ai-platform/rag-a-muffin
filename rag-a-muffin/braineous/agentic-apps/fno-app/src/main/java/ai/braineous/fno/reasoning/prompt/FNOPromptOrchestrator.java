package ai.braineous.fno.reasoning.prompt;

import ai.braineous.rag.prompt.cgo.api.*;

import ai.braineous.rag.prompt.cgo.prompt.PromptBuilder;
import ai.braineous.rag.prompt.cgo.prompt.SimpleResponseContractRegistry;

import ai.braineous.rag.prompt.cgo.query.CgoQueryPipeline;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;

import ai.braineous.rag.prompt.observe.Console;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.Map;

public class FNOPromptOrchestrator {

    public QueryExecution<ValidateTask> orchestrate(JsonObject json){
        PromptInput promptInput = new PromptInput().generate(json);
        Meta meta = promptInput.getMeta();
        ValidateTask task = promptInput.getTask();
        GraphContext context = promptInput.getGraphContext();
        String factId = task.getFactId();

        //TODO: wire validators
        PhaseResultValidator llmResponseValidator = null;
        PhaseResultValidator phaseResultValidator = null;

        QueryRequest<ValidateTask> request =
                QueryRequests.validateTask(meta, task, context, factId);

        // PromptBuilder with NO prompt validator
        PromptBuilder promptBuilder = new PromptBuilder(
                new SimpleResponseContractRegistry(),
                phaseResultValidator);

        CgoQueryPipeline pipeline = new CgoQueryPipeline(promptBuilder,
                null, //use the CGO LLMOrchestrator
                llmResponseValidator
        );

        // act
        QueryExecution<ValidateTask> execution = pipeline.execute(request);

        // console inspect
        Console.log("happy.rawResponse", execution.getRawResponse());
        Console.log("happy.promptValidation", execution.getPromptValidation());
        Console.log("happy.llmResponseValidation", execution.getLlmResponseValidation());
        Console.log("happy.domainValidation", execution.getDomainValidation());

        return execution;
    }
}
