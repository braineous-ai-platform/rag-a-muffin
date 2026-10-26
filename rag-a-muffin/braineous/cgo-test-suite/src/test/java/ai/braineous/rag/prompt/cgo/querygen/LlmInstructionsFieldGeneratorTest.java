package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class LlmInstructionsFieldGeneratorTest {

    @Test
    public void test_1() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();

        boolean supports = generator.supports(new FieldDefinition("llm_instructions"));

        Console.log("llm.instructions.supports", String.valueOf(supports));

        Assertions.assertTrue(supports);
    }

    @Test
    public void test_2() {
        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();

        boolean supports = generator.supports(new FieldDefinition("something_else"));

        Console.log("llm.instructions.supports.other", String.valueOf(supports));

        Assertions.assertFalse(supports);
    }

    @Test
    public void test_3() {
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");
        QueryRequest request = new QueryRequest();

        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject fieldValue = result.getFieldValue();
        JsonArray instructions = fieldValue.getAsJsonArray("instructions");

        Console.log("llm.instructions.generated", instructions.toString());
        int instructionIndex = 0;
        while (instructionIndex < instructions.size()) {
            Console.log(
                    "llm.instruction." + instructionIndex,
                    instructions.get(instructionIndex).getAsString()
            );
            instructionIndex = instructionIndex + 1;
        }

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(fieldValue);
        Assertions.assertTrue(fieldValue.has("instructions"));
        Assertions.assertNotNull(instructions);

        JsonArray expectedInstructions =
                loadExpectedV14Instructions();

        Assertions.assertEquals(12, expectedInstructions.size());
        Assertions.assertEquals(expectedInstructions.size(), instructions.size());

        int expectedIndex = 0;
        while (expectedIndex < expectedInstructions.size()) {
            Assertions.assertEquals(
                    expectedInstructions.get(expectedIndex).getAsString(),
                    instructions.get(expectedIndex).getAsString()
            );
            expectedIndex = expectedIndex + 1;
        }

        assertInstructionDoesNotExist(instructions, "You are an execution engine, not a document reader.");
        assertInstructionDoesNotExist(instructions, "Return only key=value pairs.");
        assertInstructionDoesNotExist(instructions, "Return exactly one result=value pair.");
        assertInstructionDoesNotExist(instructions, "The result key is mandatory.");
        assertInstructionDoesNotExist(instructions, "The final response must contain a field named result.");
        assertInstructionDoesNotExist(instructions, "The task.controls object defines a cognitive method.");
        assertInstructionDoesNotExist(instructions, "Use task.controls.method as the method name.");
        assertInstructionDoesNotExist(instructions, "Use task.controls.spec as the definition of the expression and its result.");
        assertInstructionDoesNotExist(instructions, "Use task.controls.result as the name of the output field.");
        assertInstructionDoesNotExist(instructions, "The output field named result contains the value produced by the expression.");
        assertInstructionDoesNotExist(instructions, "The reason key is optional.");
        assertInstructionDoesNotExist(instructions, "Return at most one reason=value pair.");
        assertInstructionDoesNotExist(instructions, "Use the requested SELECT fields as keys.");
        assertInstructionDoesNotExist(instructions, "Set result.decision as a string value derived from llm_query execution.");
        assertInstructionDoesNotExist(instructions, "Set result.reason as a string value derived from llm_query execution.");
        assertInstructionDoesNotExist(instructions, "Set result.code as a string value derived from llm_query execution.");

        Assertions.assertTrue(result.getValidationResult().isOk());
        Assertions.assertEquals("field.llm_instructions.ok", result.getValidationResult().getCode());
    }

    @Test
    public void test_4() {
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");

        ValidateTask task =
                new ValidateTask(
                        "decision",
                        "PaymentRequest:PAY-1001",
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList("CustomerAccount:CUST-2001", "PaymentMethod:PM-3001")
                );

        QueryRequest request = new QueryRequest(null, null, task, null, null);

        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject fieldValue = result.getFieldValue();
        JsonArray instructions = fieldValue.getAsJsonArray("instructions");

        Console.log("llm.instructions.with.fields", instructions.toString());

        assertInstructionDoesNotExist(instructions, "Set result.decision as a string value derived from llm_query execution.");
        assertInstructionDoesNotExist(instructions, "Set result.reason as a string value derived from llm_query execution.");
        assertInstructionDoesNotExist(instructions, "Set result.code as a string value derived from llm_query execution.");

        Assertions.assertTrue(result.getValidationResult().isOk());
    }

    @Test
    public void test_5() {
        FieldDefinition fieldDefinition = new FieldDefinition("llm_instructions");
        QueryRequest request = new QueryRequest();

        LlmInstructionsFieldGenerator generator = new LlmInstructionsFieldGenerator();
        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject fieldValue = result.getFieldValue();
        JsonArray instructions = fieldValue.getAsJsonArray("instructions");

        Console.log("llm.instructions.no.old.runtime.result", instructions.toString());

        assertInstructionDoesNotExist(instructions, "Use runtime_result as truth.");
        assertInstructionDoesNotExist(instructions, "Do NOT recompute validation from context.");
        assertInstructionDoesNotExist(instructions, "Do not infer control values from context.");
    }

    private void assertInstructionExists(JsonArray instructions, String expected) {
        boolean found = false;

        int i = 0;
        while (i < instructions.size()) {
            String instruction = instructions.get(i).getAsString();

            if (expected.equals(instruction)) {
                found = true;
            }

            i++;
        }

        Assertions.assertTrue(found, expected);
    }

    private void assertInstructionDoesNotExist(JsonArray instructions, String unexpected) {
        boolean found = false;

        int i = 0;
        while (i < instructions.size()) {
            String instruction = instructions.get(i).getAsString();

            if (unexpected.equals(instruction)) {
                found = true;
            }

            i++;
        }

        Assertions.assertFalse(found, unexpected);
    }

    private JsonArray loadExpectedV14Instructions() {
        InputStream stream =
                LlmInstructionsFieldGeneratorTest.class.getResourceAsStream(
                        "/ai/braineous/rag/prompt/cgo/querygen/v14-llm-instructions.json"
                );

        Assertions.assertNotNull(stream);

        JsonObject root =
                JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                ).getAsJsonObject();

        JsonArray expected =
                root.getAsJsonArray("instructions");

        Assertions.assertNotNull(expected);

        return expected;
    }
}