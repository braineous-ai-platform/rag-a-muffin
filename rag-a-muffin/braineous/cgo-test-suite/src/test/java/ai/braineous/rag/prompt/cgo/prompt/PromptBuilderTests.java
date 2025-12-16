package ai.braineous.rag.prompt.cgo.prompt;

import ai.braineous.rag.prompt.cgo.api.*;
import ai.braineous.rag.prompt.cgo.query.GsonPromptRequestValidator;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderTests {

    @Test
    void generateRequestPrompt_shouldBuildExpectedJsonStructure() {
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

        PromptBuilder builder = new PromptBuilder(new SimpleResponseContractRegistry());

        // act
        PromptRequestOutput output = builder.generateRequestPrompt(request);

        // console inspect
        Console.log("Prompt JSON Output", output.getRequestOutput());

        // assert
        assertNotNull(output, "PromptRequestOutput should not be null");
        JsonObject root = output.getRequestOutput();
        assertNotNull(root, "Root JsonObject should not be null");

        // ---- meta ----
        assertTrue(root.has("meta"), "Root JSON should contain 'meta'");
        JsonObject metaJson = root.getAsJsonObject("meta");
        assertEquals("v1", metaJson.get("version").getAsString());
        assertEquals("validate_flight_airports", metaJson.get("query_kind").getAsString());
        assertEquals(meta.getDescription(), metaJson.get("description").getAsString());

        // ---- context.nodes ----
        assertTrue(root.has("context"), "Root JSON should contain 'context'");
        JsonObject contextJson = root.getAsJsonObject("context");
        assertTrue(contextJson.has("nodes"), "Context should contain 'nodes'");

        JsonObject nodesJson = contextJson.getAsJsonObject("nodes");
        assertTrue(nodesJson.has(factId), "Nodes should contain the factId as key");

        JsonObject nodeJson = nodesJson.getAsJsonObject(factId);
        assertEquals(factId, nodeJson.get("id").getAsString(), "Node id should match");
        assertEquals(node.getText(), nodeJson.get("text").getAsString(), "Node text should match");
        assertTrue(nodeJson.has("attributes"), "Node should contain 'attributes'");
        assertTrue(nodeJson.getAsJsonArray("attributes").isEmpty(), "Attributes should be empty for this test node");
        assertEquals(node.getMode().name().toLowerCase(), nodeJson.get("mode").getAsString());

        // ---- task ----
        assertTrue(root.has("task"), "Root JSON should contain 'task'");
        JsonObject taskJson = root.getAsJsonObject("task");
        assertEquals(taskDescription, taskJson.get("description").getAsString());
        assertEquals(factId, taskJson.get("factId").getAsString());

        // ---- instructions ----
        assertTrue(root.has("instructions"), "Root JSON should contain 'instructions'");
        JsonArray instructions = root.getAsJsonArray("instructions");
        assertEquals(3, instructions.size(), "There should be exactly 3 generic instructions");

        assertEquals(
                "Return a single JSON object that strictly follows this schema.",
                instructions.get(0).getAsString()
        );
        assertEquals(
                "Do not include any fields not listed in this schema.",
                instructions.get(1).getAsString()
        );
        assertEquals(
                "Do not add natural language outside of JSON.",
                instructions.get(2).getAsString()
        );

        // ---- response_contract ----
        assertTrue(root.has("response_contract"), "Root JSON should contain 'response_contract'");
        JsonObject rc = root.getAsJsonObject("response_contract");
        assertEquals("validation_result", rc.get("type").getAsString());
        assertTrue(rc.has("schema"), "response_contract should contain 'schema'");

        // ---- llm_instructions ----
        assertTrue(root.has("llm_instructions"), "Root JSON should contain 'llm_instructions'");
        JsonArray llmInstructions = root.getAsJsonArray("llm_instructions");
        assertFalse(llmInstructions.isEmpty(), "llm_instructions should not be empty for validate_flight_airports");
        assertEquals(
                "You are given a JSON object with 'meta', 'context', 'task', 'response_contract', and 'llm_instructions' fields.",
                llmInstructions.get(0).getAsString()
        );
    }

    @Test
    void generateRequestPrompt_withValidator_shouldAttachValidationResult() {
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

        // capture what the validator sees
        final String[] capturedJson = new String[1];

        PhaseResultValidator fakeValidator = raw -> {
            capturedJson[0] = raw;
            return ValidationResult.createInternal(
                    true,
                    "prompt.contract.ok",
                    "Prompt contract valid (fake validator)",
                    "prompt_contract_validation",
                    null,
                    Map.of("phase", "prompt_builder_test")
            );
        };

        PromptBuilder builder = new PromptBuilder(new SimpleResponseContractRegistry(), fakeValidator);

        // act
        PromptRequestOutput output = builder.generateRequestPrompt(request);

        // console inspect
        Console.log("Prompt JSON Output (with validator)", output.getRequestOutput());
        Console.log("Prompt ValidationResult", output.getValidationResult());
        Console.log("Captured JSON in FakeValidator", capturedJson[0]);

        // assert
        assertNotNull(output, "PromptRequestOutput should not be null");
        assertNotNull(output.getRequestOutput(), "Request JSON should not be null");

        ValidationResult validationResult = output.getValidationResult();
        assertNotNull(validationResult, "ValidationResult should be attached when validator is provided");
        assertTrue(validationResult.isOk(), "Expected ValidationResult.isOk() to be true");
        assertEquals("prompt.contract.ok", validationResult.getCode());
        assertEquals("prompt_contract_validation", validationResult.getStage());

        assertNotNull(capturedJson[0], "FakeValidator should have received the prompt JSON");
        assertFalse(capturedJson[0].isBlank(), "Captured JSON should not be blank");
    }

    @Test
    void generateRequestPrompt_withValidatorError_shouldAttachErrorValidationResult() {
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

        PhaseResultValidator fakeValidator = raw ->
                ValidationResult.createInternal(
                        false,
                        "prompt.contract.error",
                        "Prompt contract invalid (fake validator error)",
                        "prompt_contract_validation",
                        null,
                        Map.of("phase", "prompt_builder_test_error")
                );

        PromptBuilder builder = new PromptBuilder(new SimpleResponseContractRegistry(), fakeValidator);

        // act
        PromptRequestOutput output = builder.generateRequestPrompt(request);

        // console inspect
        Console.log("Prompt JSON Output (validator error)", output.getRequestOutput());
        Console.log("Prompt ValidationResult (error)", output.getValidationResult());

        // assert
        assertNotNull(output, "PromptRequestOutput should not be null");
        assertNotNull(output.getRequestOutput(), "Request JSON should not be null");

        ValidationResult validationResult = output.getValidationResult();
        assertNotNull(validationResult, "ValidationResult should be attached when validator is provided");
        assertFalse(validationResult.isOk(), "Expected ValidationResult.isOk() to be false");
        assertEquals("prompt.contract.error", validationResult.getCode());
        assertEquals("prompt_contract_validation", validationResult.getStage());
        assertEquals("Prompt contract invalid (fake validator error)", validationResult.getMessage());
    }

    @Test
    void generateRequestPrompt_withRealValidator_shouldProduceOkValidationResult() {
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

        // real validator wired through PhaseResultValidator
        GsonPromptRequestValidator realValidator = new GsonPromptRequestValidator();
        PhaseResultValidator phaseValidator = realValidator::validate;

        PromptBuilder builder = new PromptBuilder(new SimpleResponseContractRegistry(), phaseValidator);

        // act
        PromptRequestOutput output = builder.generateRequestPrompt(request);

        Console.log("Prompt JSON Output (real validator)", output.getRequestOutput());
        Console.log("Prompt ValidationResult (real validator)", output.getValidationResult());

        // assert
        assertNotNull(output, "PromptRequestOutput should not be null");
        assertNotNull(output.getRequestOutput(), "Request JSON should not be null");

        ValidationResult validationResult = output.getValidationResult();
        assertNotNull(validationResult, "ValidationResult should be attached in validation mode");
        assertTrue(validationResult.isOk(), "Expected ValidationResult.isOk() to be true");
        assertEquals("prompt.contract.ok", validationResult.getCode());
        assertEquals("prompt_contract_validation", validationResult.getStage());
    }

    @Test
    void generateRequestPrompt_withRealValidatorAndTamperedJson_shouldAttachErrorValidationResult() {
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

        // real validator
        GsonPromptRequestValidator realValidator = new GsonPromptRequestValidator();

        // PhaseResultValidator that TAMPERS the JSON before delegating
        PhaseResultValidator phaseValidator = raw -> {
            JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
            // break the contract on purpose: remove 'meta'
            obj.remove("meta");
            String tampered = obj.toString();

            Console.log("Tampered Prompt JSON", tampered);

            return realValidator.validate(tampered);
        };

        PromptBuilder builder = new PromptBuilder(new SimpleResponseContractRegistry(), phaseValidator);

        // act
        PromptRequestOutput output = builder.generateRequestPrompt(request);

        Console.log("Prompt JSON Output (original)", output.getRequestOutput());
        Console.log("Prompt ValidationResult (real validator, tampered)", output.getValidationResult());

        // assert
        assertNotNull(output, "PromptRequestOutput should not be null");
        assertNotNull(output.getRequestOutput(), "Original request JSON should not be null");

        ValidationResult validationResult = output.getValidationResult();
        assertNotNull(validationResult, "ValidationResult should be attached in validation mode");
        assertFalse(validationResult.isOk(), "Expected ValidationResult.isOk() to be false");
        assertEquals("prompt.contract.meta_missing_or_invalid", validationResult.getCode());
        assertEquals("prompt_contract_validation", validationResult.getStage());
    }
}

