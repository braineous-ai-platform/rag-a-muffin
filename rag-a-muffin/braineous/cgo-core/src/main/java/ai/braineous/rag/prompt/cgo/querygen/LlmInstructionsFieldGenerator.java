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

        instructions.add("You execute the declarative program supplied in task.controls using the data supplied in context.");
        instructions.add("Return only result=<computed runtime value>. Do not output explanations, JSON, the declarative program, context, or any other text.");
        instructions.add("task.controls is a declarative program whose execution consists of resolving its parameters from context, evaluating its expression, applying its spec, and producing its result; no external code or implementation is required.");
        instructions.add("Each field in task.controls represents a programming primitive.");
        instructions.add("Execute the declarative program according to the defined semantics of its primitives.");
        instructions.add("Do not describe or summarize the declarative program.");
        instructions.add("method identifies the operation being executed; it is not a function declaration or a reference to an external implementation.");
        instructions.add("parameters identifies the input fields required by the operation; each parameter is a reference to a field in the supplied runtime context, not a literal value.");
        instructions.add("expression defines the computation to execute using the values resolved from parameters; evaluate its operators and conditions to produce a computed value, rather than returning or describing the expression text.");
        instructions.add("spec defines the developer-supplied semantic contract for interpreting the computed expression value and determining the required runtime result; apply this contract after evaluating expression.");
        instructions.add("result identifies the output field of the operation; populate this field with the runtime value determined by applying spec to the evaluated expression, rather than treating the field's declared value as the computed result.");
        instructions.add("All generated responses must be in English.");
    }
}