package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class PromptInput {

    private Meta meta;

    private GraphContext graphContext;

    private ValidateTask task;

    private ResponseContract responseContract;

    private List<Instruction> instructions = new ArrayList<>();

    private List<LlmInstruction> llmInstructions = new ArrayList<>();


    public PromptInput() {

    }

    public PromptInput(Meta meta, GraphContext graphContext, ValidateTask task, ResponseContract responseContract, List<Instruction> instructions, List<LlmInstruction> llmInstructions) {
        this.meta = meta;
        this.graphContext = graphContext;
        this.task = task;
        this.responseContract = responseContract;
        this.instructions = instructions;
        this.llmInstructions = llmInstructions;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public GraphContext getGraphContext() {
        return graphContext;
    }

    public void setGraphContext(GraphContext graphContext) {
        this.graphContext = graphContext;
    }

    public ValidateTask getTask() {
        return task;
    }

    public void setTask(ValidateTask task) {
        this.task = task;
    }

    public ResponseContract getResponseContract() {
        return responseContract;
    }

    public void setResponseContract(ResponseContract responseContract) {
        this.responseContract = responseContract;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public List<LlmInstruction> getLlmInstructions() {
        return llmInstructions;
    }

    public void setLlmInstructions(List<LlmInstruction> llmInstructions) {
        this.llmInstructions = llmInstructions;
    }

    public PromptInput generate(JsonObject jsonObject) {
        PromptInput prompt = new PromptInput();

        if (jsonObject == null) {
            return prompt;
        }

        // 1) META
        if (jsonObject.has("meta") && jsonObject.get("meta").isJsonObject()) {
            JsonObject metaObj = jsonObject.getAsJsonObject("meta");

            String version = metaObj.has("version") ? metaObj.get("version").getAsString() : null;
            String queryKind = metaObj.has("query_kind") ? metaObj.get("query_kind").getAsString() : null;
            String description = metaObj.has("description") ? metaObj.get("description").getAsString() : null;

            Meta meta = new Meta(version, queryKind, description);
            prompt.setMeta(meta);
        }

        // 2) GRAPH CONTEXT
        if (jsonObject.has("context") && jsonObject.get("context").isJsonObject()) {
            JsonObject contextObj = jsonObject.getAsJsonObject("context");

            // assuming a static factory
            GraphContext graphContext = GraphContext.fromJson(contextObj);
            prompt.setGraphContext(graphContext);
        }

        // 3) TASK
        if (jsonObject.has("task") && jsonObject.get("task").isJsonObject()) {
            JsonObject taskObj = jsonObject.getAsJsonObject("task");

            // assuming a static factory
            ValidateTask task = ValidateTask.fromJson(taskObj);
            prompt.setTask(task);
        }

        // 4) RESPONSE CONTRACT
        if (jsonObject.has("response_contract") && jsonObject.get("response_contract").isJsonObject()) {
            JsonObject rcObj = jsonObject.getAsJsonObject("response_contract");

            ResponseContract responseContract = ResponseContract.fromJson(rcObj);
            prompt.setResponseContract(responseContract);
        }

        // 5) INSTRUCTIONS (system / hard)
        if (jsonObject.has("instructions") && jsonObject.get("instructions").isJsonArray()) {
            List<Instruction> instructions = new ArrayList<>();

            jsonObject.getAsJsonArray("instructions").forEach(elem -> {
                if (elem.isJsonPrimitive()) {
                    instructions.add(new Instruction(elem.getAsString()));
                }
            });

            prompt.setInstructions(instructions);
        }

        // 6) LLM INSTRUCTIONS (reasoning guidance)
        if (jsonObject.has("llm_instructions") && jsonObject.get("llm_instructions").isJsonArray()) {
            List<LlmInstruction> llmInstructions = new ArrayList<>();

            jsonObject.getAsJsonArray("llm_instructions").forEach(elem -> {
                if (elem.isJsonPrimitive()) {
                    llmInstructions.add(new LlmInstruction(elem.getAsString()));
                }
            });

            prompt.setLlmInstructions(llmInstructions);
        }

        return prompt;
    }

    @Override
    public String toString() {
        return "PromptInput{" +
                "meta=" + meta +
                ", graphContext=" + graphContext +
                ", task=" + task +
                ", responseContract=" + responseContract +
                ", instructions=" + instructions +
                ", llmInstructions=" + llmInstructions +
                '}';
    }
}
