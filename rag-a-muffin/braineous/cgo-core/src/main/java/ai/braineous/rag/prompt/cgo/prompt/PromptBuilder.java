package ai.braineous.rag.prompt.cgo.prompt;

import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.GsonPromptRequestValidator;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public class PromptBuilder {

    private final ResponseContractRegistry registry;
    private final PhaseResultValidator validator;

    public PromptBuilder() {
        this.registry = new SimpleResponseContractRegistry();
        this.validator = new GsonPromptRequestValidator();
    }

    public PromptBuilder(ResponseContractRegistry registry) {
        this(registry, new GsonPromptRequestValidator());
    }

    public PromptBuilder(ResponseContractRegistry registry, PhaseResultValidator validator) {
        this.registry = registry;
        this.validator = validator;
    }

    public PromptRequestOutput generateRequestPrompt(QueryRequest<?> request) {
        JsonObject root = new JsonObject();

        JsonObject meta = new JsonObject();
        meta.addProperty("version", request.getMeta().getVersion());
        meta.addProperty("query_kind", request.getMeta().getQueryKind());
        meta.addProperty("description", request.getMeta().getDescription());
        root.add("meta", meta);

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

        JsonObject taskJson = new JsonObject();
        taskJson.addProperty("description", request.getTask().getDescription());
        if (request.getTask() instanceof ValidateTask) {
            ValidateTask vt = (ValidateTask) request.getTask();
            taskJson.addProperty("factId", vt.getFactId());
        }
        root.add("task", taskJson);

        JsonObject responseContractJson =
                registry.responseContractFor(request.getMeta().getQueryKind());
        root.add("response_contract", responseContractJson);

        root.add("instructions", toJsonArray(List.of(
                "Return a single JSON object that strictly follows this schema.",
                "Do not include any fields not listed in this schema.",
                "Do not add natural language outside of JSON."
        )));

        root.add(
                "llm_instructions",
                toJsonArray(registry.llmInstructionsFor(request.getMeta().getQueryKind()))
        );

        ValidationResult result = this.validatePromptOutput(root);

        return new PromptRequestOutput(root, result);
    }

    public PromptRequestOutput generateExecutionPrompt(JsonObject llmQuery) {
        String promptText = buildExecutionPromptText(llmQuery);

        JsonObject root = new JsonObject();
        root.addProperty("prompt", promptText);

        ValidationResult result =
                ValidationResult.ok(
                        "prompt.execution.ok",
                        "Execution prompt generated"
                );

        return new PromptRequestOutput(root, result);
    }

    private String buildExecutionPromptText(JsonObject llmQuery) {
        StringBuilder prompt = new StringBuilder();

        appendInstructions(prompt, llmQuery);

        prompt.append("\n");
        prompt.append("INPUT:\n");
        prompt.append(buildExecutionInput(llmQuery).toString());

        return prompt.toString();
    }

    private void appendInstructions(StringBuilder prompt, JsonObject llmQuery) {
        JsonArray instructions = null;

        if (llmQuery != null
                && llmQuery.has("llm_instructions")
                && llmQuery.get("llm_instructions").isJsonObject()) {

            JsonObject llmInstructions =
                    llmQuery.getAsJsonObject("llm_instructions");

            if (llmInstructions.has("instructions")
                    && llmInstructions.get("instructions").isJsonArray()) {
                instructions =
                        llmInstructions.getAsJsonArray("instructions");
            }
        }

        if (instructions == null) {
            return;
        }

        int i = 0;
        while (i < instructions.size()) {
            if (!instructions.get(i).isJsonNull()) {
                prompt.append(instructions.get(i).getAsString());
                prompt.append("\n");
            }

            i++;
        }
    }

    private JsonObject buildExecutionInput(JsonObject llmQuery) {
        JsonObject input = new JsonObject();

        addIfPresent(input, llmQuery, "task");
        addIfPresent(input, llmQuery, "context");
        addIfPresent(input, llmQuery, "output_template");

        return input;
    }

    private void addIfPresent(JsonObject target, JsonObject source, String fieldName) {
        if (source == null) {
            return;
        }

        if (!source.has(fieldName)) {
            return;
        }

        target.add(fieldName, source.get(fieldName));
    }

    private ValidationResult validatePromptOutput(JsonObject root) {
        if (this.validator != null) {
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