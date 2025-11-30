package ai.braineous.rag.prompt.cgo.prompt;

import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public class PromptBuilder {

    private final ResponseContractRegistry registry;
    private final PhaseResultValidator validator;

    public PromptBuilder(ResponseContractRegistry registry) {
        this(registry, null);
    }

    public PromptBuilder(ResponseContractRegistry registry, PhaseResultValidator validator) {
        this.registry = registry;
        this.validator = validator;
    }

    public PromptRequestOutput generateRequestPrompt(QueryRequest<?> request) {
        JsonObject root = new JsonObject();

        // meta
        JsonObject meta = new JsonObject();
        meta.addProperty("version", request.getMeta().getVersion());
        meta.addProperty("query_kind", request.getMeta().getQueryKind());
        meta.addProperty("description", request.getMeta().getDescription());
        root.add("meta", meta);

        // context
        JsonObject context = new JsonObject();
        JsonObject nodesJson = new JsonObject();
        request.getContext().getNodes().forEach((id, node) -> {
            JsonObject nodeJson = new JsonObject();
            nodeJson.addProperty("id", node.getId());
            nodeJson.addProperty("text", node.getText());
            nodeJson.add("attributes", toJsonArray(node.getAttributes()));
            nodeJson.addProperty("mode", node.getMode().name().toLowerCase());
            nodesJson.add(id, nodeJson);
        });
        context.add("nodes", nodesJson);
        root.add("context", context);

        // task
        JsonObject taskJson = new JsonObject();
        taskJson.addProperty("description", request.getTask().getDescription());
        if (request.getTask() instanceof ValidateTask vt) {
            taskJson.addProperty("factId", vt.getFactId());
        }
        root.add("task", taskJson);

        // response_contract from registry
        JsonObject responseContractJson =
                registry.responseContractFor(request.getMeta().getQueryKind());
        root.add("response_contract", responseContractJson);

        // generic instructions
        root.add("instructions", toJsonArray(List.of(
                "Return a single JSON object that strictly follows this schema.",
                "Do not include any fields not listed in this schema.",
                "Do not add natural language outside of JSON."
        )));

        // llm_instructions per queryKind from registry
        root.add("llm_instructions",
                toJsonArray(registry.llmInstructionsFor(request.getMeta().getQueryKind())));

        //validation phase
        ValidationResult result = this.validatePromptOutput(root);

        return new PromptRequestOutput(root, result);
    }

    private ValidationResult validatePromptOutput(JsonObject root){
        if(this.validator != null){
            return this.validator.validate(root.toString());
        }
        return null;
    }

    private JsonElement toJsonArray(List<String> items) {
        JsonArray arr = new JsonArray();
        for (String s : items) {
            arr.add(s);
        }
        return arr;
    }
}

