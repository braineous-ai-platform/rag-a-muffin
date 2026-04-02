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

        /*instructions.add("Return exactly one JSON object.");
        instructions.add("Use response_contract.schema.result.fields to construct the result object.");
        instructions.add("Place all generated result values under the result field.");
        instructions.add("Do not modify the response_contract section.");
        instructions.add("Do not add any fields not defined in response_contract.schema.result.fields.");
        instructions.add("Do not remove any fields defined in response_contract.schema.result.fields.");
        instructions.add("Do not rename any fields.");
        instructions.add("Set every returned field value as a string.");
        instructions.add("If a value cannot be determined, return an empty string for that field.");
        instructions.add("Do not include natural language outside the JSON object.");*/

        /*instructions.add("You are executing a task, not explaining the input.");
        instructions.add("Return exactly one JSON object and nothing else.");
        instructions.add("Do not explain, describe, summarize, or restate the input.");
        instructions.add("Use response_contract.schema.result.fields to construct the result object.");
        instructions.add("Place all generated result values under the result field.");
        instructions.add("Do not modify the response_contract section.");
        instructions.add("Do not add any fields not defined in response_contract.schema.result.fields.");
        instructions.add("Do not remove any fields defined in response_contract.schema.result.fields.");
        instructions.add("Do not rename any fields.");
        instructions.add("Set every returned field value as a string.");
        instructions.add("If a value cannot be determined, return an empty string for that field.");
        instructions.add("Output must be valid JSON only.");*/

        instructions.add("Execute the task and return the solution object.");
        instructions.add("Your output must be exactly one JSON object.");
        instructions.add("The top-level JSON object must contain only these keys: result and trace.");
        instructions.add("Construct result using response_contract.schema.result.fields.");
        instructions.add("Construct trace using llm_trace.schema.trace.fields.");
        instructions.add("Fill each field with a string value.");
        instructions.add("If a field value cannot be determined from the provided context, return an empty string for that field.");
        instructions.add("Do not describe the prompt, task, schema, or input.");
        instructions.add("Do not explain your reasoning.");
        instructions.add("Do not output markdown.");
        instructions.add("Do not output prose.");
        instructions.add("Do not output any keys other than result and trace.");
        instructions.add("Return JSON only.");

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
}
