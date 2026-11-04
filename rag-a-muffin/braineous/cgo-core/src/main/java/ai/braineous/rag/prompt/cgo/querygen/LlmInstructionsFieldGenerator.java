package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class LlmInstructionsFieldGenerator implements FieldGenerator {

    public LlmInstructionsFieldGenerator() {
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }

        if (fieldDefinition.getName() == null) {
            return false;
        }

        return "llm_instructions".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request) {
        JsonObject fieldValue = new JsonObject();
        JsonArray instructions = new JsonArray();

        addExecutionModeInstructions(instructions);

        fieldValue.add("instructions", instructions);

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.llm_instructions.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);
    }

    private void addExecutionModeInstructions(JsonArray instructions) {
        // instructions.add("You are an execution engine, not a document reader.");
        // instructions.add("Return only JSON.");
        // instructions.add("Use the provided output_template as the final answer format.");
        // instructions.add("Execute the llm_query using only the provided context and task.");
        // instructions.add("Do not describe, summarize, explain, or analyze this request.");
        // instructions.add("Use context.nodes as the system state.");
        // instructions.add("Use task.factId as the primary fact.");
        // instructions.add("Use task.relatedFactIds as related system facts.");
        // instructions.add("Use task.controls as execution controls only.");
        // instructions.add("Do not treat task.controls as additional facts.");
        // instructions.add("Do not infer missing facts from task.controls.");
        // instructions.add("Do not recompute task.controls from context.");
        // instructions.add("Return compact JSON on a single line.");
        // instructions.add("Do not include spaces, tabs, or newlines outside JSON syntax.");
        // instructions.add("Set every value in output_template as a string.");
        // instructions.add("Return exactly the output_template shape.");
        // instructions.add("Do not add, remove, or rename any fields.");
        // instructions.add("Return exactly one JSON object.");
        // instructions.add("Do not wrap the JSON in markdown fences.");
        // instructions.add("Do not include explanation before or after the JSON.");

        instructions.add("execute a function");
        instructions.add("context contains its runtime state");
        instructions.add("task.controls.parameters are references to values in context");
        instructions.add("task.controls.expression is to be calculated, using values from task.controls.parameters. return is the outcome of the calculation. return will be an input to task.controls.spec");
        instructions.add("task.controls.spec executes in 2 phases:");
        instructions.add("phase1=Definitions");
        instructions.add("phase2=Resolution");
        instructions.add("execute task.controls.spec phase1 and phase2 in order. return task.controls.result = result");
        instructions.add("respond only with task.controls.result value");
        instructions.add("stop immediately after you respond");
        instructions.add("All generated responses must be in English.");
    }
}