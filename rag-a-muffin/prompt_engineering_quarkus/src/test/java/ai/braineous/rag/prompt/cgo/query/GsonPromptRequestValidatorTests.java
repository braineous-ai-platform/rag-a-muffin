package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GsonPromptRequestValidatorTests {

    @Test
    void validate_withValidPrompt_shouldReturnOk() {
        // arrange
        JsonObject meta = new JsonObject();
        meta.addProperty("version", "v1");
        meta.addProperty("query_kind", "validate_flight_airports");
        meta.addProperty("description", "Validate that the selected flight has valid airports.");

        JsonObject context = new JsonObject();
        JsonObject nodes = new JsonObject();
        // minimal but valid
        context.add("nodes", nodes);

        JsonObject task = new JsonObject();
        task.addProperty("description", "Validate that the selected flight has valid airports.");
        task.addProperty("factId", "Flight:F100");

        JsonObject responseContract = new JsonObject();
        responseContract.addProperty("type", "validation_result");
        responseContract.addProperty("description", "Standard response for fact-level validation.");

        JsonArray instructions = new JsonArray();
        instructions.add("Return a single JSON object that strictly follows this schema.");
        instructions.add("Do not include any fields not listed in this schema.");
        instructions.add("Do not add natural language outside of JSON.");

        JsonArray llmInstructions = new JsonArray();
        llmInstructions.add("Validate the flight fact using the provided context nodes.");
        llmInstructions.add("Ensure from/to airports exist and are different.");

        JsonObject root = new JsonObject();
        root.add("meta", meta);
        root.add("context", context);
        root.add("task", task);
        root.add("response_contract", responseContract);
        root.add("instructions", instructions);
        root.add("llm_instructions", llmInstructions);

        String promptJson = root.toString();

        Console.log("Prompt JSON", promptJson);

        GsonPromptRequestValidator validator = new GsonPromptRequestValidator();

        // act
        ValidationResult result = validator.validate(promptJson);

        // console dumps following your cadence
        Console.log("result.ok", result.isOk());
        Console.log("result.code", result.getCode());
        Console.log("result.message", result.getMessage());
        Console.log("result.stage", result.getStage());
        Console.log("result.anchorId", result.getAnchorId());
        Console.log("result.metadata", result.getMetadata());

        // assert
        assertTrue(result.isOk(), "Expected prompt contract to be valid");
        assertEquals("prompt.contract.ok", result.getCode());
        assertEquals("prompt_contract_validation", result.getStage());
    }

    @Test
    void validate_missingMetaVersion_shouldReturnError() {
        // arrange
        JsonObject meta = new JsonObject();
        // meta.addProperty("version", "v1"); // intentionally missing
        meta.addProperty("query_kind", "validate_flight_airports");
        meta.addProperty("description", "Validate that the selected flight has valid airports.");

        JsonObject context = new JsonObject();
        JsonObject nodes = new JsonObject();
        context.add("nodes", nodes);

        JsonObject task = new JsonObject();
        task.addProperty("description", "Validate that the selected flight has valid airports.");
        task.addProperty("factId", "Flight:F100");

        JsonObject responseContract = new JsonObject();
        responseContract.addProperty("type", "validation_result");
        responseContract.addProperty("description", "Standard response for fact-level validation.");

        JsonArray instructions = new JsonArray();
        instructions.add("Return a single JSON object that strictly follows this schema.");
        instructions.add("Do not include any fields not listed in this schema.");
        instructions.add("Do not add natural language outside of JSON.");

        JsonArray llmInstructions = new JsonArray();
        llmInstructions.add("Validate the flight fact using the provided context nodes.");
        llmInstructions.add("Ensure from/to airports exist and are different.");

        JsonObject root = new JsonObject();
        root.add("meta", meta);
        root.add("context", context);
        root.add("task", task);
        root.add("response_contract", responseContract);
        root.add("instructions", instructions);
        root.add("llm_instructions", llmInstructions);

        String promptJson = root.toString();
        Console.log("Prompt JSON (missing meta.version)", promptJson);

        GsonPromptRequestValidator validator = new GsonPromptRequestValidator();

        // act
        ValidationResult result = validator.validate(promptJson);

        Console.log("result.ok", result.isOk());
        Console.log("result.code", result.getCode());
        Console.log("result.message", result.getMessage());
        Console.log("result.stage", result.getStage());
        Console.log("result.metadata", result.getMetadata());

        // assert
        assertFalse(result.isOk(), "Expected validation to fail when meta.version is missing");
        assertEquals("prompt.contract.meta.version_missing", result.getCode());
        assertEquals("prompt_contract_validation", result.getStage());
    }

    @Test
    void validate_missingContextNodes_shouldReturnError() {
        // arrange
        JsonObject meta = new JsonObject();
        meta.addProperty("version", "v1");
        meta.addProperty("query_kind", "validate_flight_airports");
        meta.addProperty("description", "Validate that the selected flight has valid airports.");

        JsonObject context = new JsonObject();
        // context.add("nodes", new JsonObject()); // intentionally missing

        JsonObject task = new JsonObject();
        task.addProperty("description", "Validate that the selected flight has valid airports.");
        task.addProperty("factId", "Flight:F100");

        JsonObject responseContract = new JsonObject();
        responseContract.addProperty("type", "validation_result");
        responseContract.addProperty("description", "Standard response for fact-level validation.");

        JsonArray instructions = new JsonArray();
        instructions.add("Return a single JSON object that strictly follows this schema.");
        instructions.add("Do not include any fields not listed in this schema.");
        instructions.add("Do not add natural language outside of JSON.");

        JsonArray llmInstructions = new JsonArray();
        llmInstructions.add("Validate the flight fact using the provided context nodes.");
        llmInstructions.add("Ensure from/to airports exist and are different.");

        JsonObject root = new JsonObject();
        root.add("meta", meta);
        root.add("context", context);
        root.add("task", task);
        root.add("response_contract", responseContract);
        root.add("instructions", instructions);
        root.add("llm_instructions", llmInstructions);

        String promptJson = root.toString();
        Console.log("Prompt JSON (missing context.nodes)", promptJson);

        GsonPromptRequestValidator validator = new GsonPromptRequestValidator();

        // act
        ValidationResult result = validator.validate(promptJson);

        Console.log("result.ok", result.isOk());
        Console.log("result.code", result.getCode());
        Console.log("result.message", result.getMessage());
        Console.log("result.stage", result.getStage());
        Console.log("result.metadata", result.getMetadata());

        // assert
        assertFalse(result.isOk(), "Expected validation to fail when context.nodes is missing");
        assertEquals("prompt.contract.context.nodes_missing_or_invalid", result.getCode());
        assertEquals("prompt_contract_validation", result.getStage());
    }

    @Test
    void validate_instructionsNotAllStrings_shouldReturnError() {
        // arrange
        JsonObject meta = new JsonObject();
        meta.addProperty("version", "v1");
        meta.addProperty("query_kind", "validate_flight_airports");
        meta.addProperty("description", "Validate that the selected flight has valid airports.");

        JsonObject context = new JsonObject();
        JsonObject nodes = new JsonObject();
        context.add("nodes", nodes);

        JsonObject task = new JsonObject();
        task.addProperty("description", "Validate that the selected flight has valid airports.");
        task.addProperty("factId", "Flight:F100");

        JsonObject responseContract = new JsonObject();
        responseContract.addProperty("type", "validation_result");
        responseContract.addProperty("description", "Standard response for fact-level validation.");

        // intentionally incorrect instructions: includes a non-string element
        JsonArray instructions = new JsonArray();
        instructions.add("Return a single JSON object that strictly follows this schema.");
        instructions.add("Do not include any fields not listed in this schema.");
        JsonObject notAString = new JsonObject();
        notAString.addProperty("oops", "I am an object, not a string");
        instructions.add(notAString); // 🚨 this breaks the contract

        JsonArray llmInstructions = new JsonArray();
        llmInstructions.add("Validate the flight fact using the provided context nodes.");
        llmInstructions.add("Ensure from/to airports exist and are different.");

        JsonObject root = new JsonObject();
        root.add("meta", meta);
        root.add("context", context);
        root.add("task", task);
        root.add("response_contract", responseContract);
        root.add("instructions", instructions);
        root.add("llm_instructions", llmInstructions);

        String promptJson = root.toString();
        Console.log("Prompt JSON (invalid instructions)", promptJson);

        GsonPromptRequestValidator validator = new GsonPromptRequestValidator();

        // act
        ValidationResult result = validator.validate(promptJson);

        Console.log("result.ok", result.isOk());
        Console.log("result.code", result.getCode());
        Console.log("result.message", result.getMessage());
        Console.log("result.stage", result.getStage());
        Console.log("result.metadata", result.getMetadata());

        // assert
        assertFalse(result.isOk(), "Expected validation to fail when instructions contains non-string elements");
        assertEquals("prompt.contract.instructions_not_all_strings", result.getCode());
        assertEquals("prompt_contract_validation", result.getStage());
    }

    @Test
    void validate_llmInstructionsNotAllStrings_shouldReturnError() {
        // arrange
        JsonObject meta = new JsonObject();
        meta.addProperty("version", "v1");
        meta.addProperty("query_kind", "validate_flight_airports");
        meta.addProperty("description", "Validate that the selected flight has valid airports.");

        JsonObject context = new JsonObject();
        JsonObject nodes = new JsonObject();
        context.add("nodes", nodes);

        JsonObject task = new JsonObject();
        task.addProperty("description", "Validate that the selected flight has valid airports.");
        task.addProperty("factId", "Flight:F100");

        JsonObject responseContract = new JsonObject();
        responseContract.addProperty("type", "validation_result");
        responseContract.addProperty("description", "Standard response for fact-level validation.");

        JsonArray instructions = new JsonArray();
        instructions.add("Return a single JSON object that strictly follows this schema.");
        instructions.add("Do not include any fields not listed in this schema.");
        instructions.add("Do not add natural language outside of JSON.");

        // intentionally incorrect llm_instructions: includes a non-string element
        JsonArray llmInstructions = new JsonArray();
        llmInstructions.add("Validate the flight fact using the provided context nodes.");
        JsonObject notAString = new JsonObject();
        notAString.addProperty("oops", "I am an object, not a string");
        llmInstructions.add(notAString); // 🚨 breaks contract

        JsonObject root = new JsonObject();
        root.add("meta", meta);
        root.add("context", context);
        root.add("task", task);
        root.add("response_contract", responseContract);
        root.add("instructions", instructions);
        root.add("llm_instructions", llmInstructions);

        String promptJson = root.toString();
        Console.log("Prompt JSON (invalid llm_instructions)", promptJson);

        GsonPromptRequestValidator validator = new GsonPromptRequestValidator();

        // act
        ValidationResult result = validator.validate(promptJson);

        Console.log("result.ok", result.isOk());
        Console.log("result.code", result.getCode());
        Console.log("result.message", result.getMessage());
        Console.log("result.stage", result.getStage());
        Console.log("result.metadata", result.getMetadata());

        // assert
        assertFalse(result.isOk(), "Expected validation to fail when llm_instructions contains non-string elements");
        assertEquals("prompt.contract.llm_instructions_not_all_strings", result.getCode());
        assertEquals("prompt_contract_validation", result.getStage());
    }
}
