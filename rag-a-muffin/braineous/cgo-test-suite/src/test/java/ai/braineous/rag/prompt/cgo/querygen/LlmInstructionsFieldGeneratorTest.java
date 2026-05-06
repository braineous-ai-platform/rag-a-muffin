package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(fieldValue);
        Assertions.assertNotNull(instructions);

        assertInstructionExists(instructions, "You are an execution engine, not a document reader.");
        assertInstructionExists(instructions, "Return only JSON.");
        assertInstructionExists(instructions, "Use the provided output_template as the final answer format.");
        assertInstructionExists(instructions, "Execute the llm_query using only the provided context and task.");
        assertInstructionExists(instructions, "Do not describe, summarize, explain, or analyze this request.");
        assertInstructionExists(instructions, "Use context.nodes as the system state.");
        assertInstructionExists(instructions, "Use task.factId as the primary fact.");
        assertInstructionExists(instructions, "Use task.relatedFactIds as related system facts.");
        assertInstructionExists(instructions, "Use task.controls as execution controls only.");
        assertInstructionExists(instructions, "Do not treat task.controls as additional facts.");
        assertInstructionExists(instructions, "Do not infer missing facts from task.controls.");
        assertInstructionExists(instructions, "Do not recompute task.controls from context.");
        assertInstructionExists(instructions, "Return compact JSON on a single line.");
        assertInstructionExists(instructions, "Do not include spaces, tabs, or newlines outside JSON syntax.");
        assertInstructionExists(instructions, "Set every value in output_template as a string.");
        assertInstructionExists(instructions, "Return exactly the output_template shape.");
        assertInstructionExists(instructions, "Do not add, remove, or rename any fields.");
        assertInstructionExists(instructions, "Return exactly one JSON object.");
        assertInstructionExists(instructions, "Do not wrap the JSON in markdown fences.");
        assertInstructionExists(instructions, "Do not include explanation before or after the JSON.");

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

        assertInstructionExists(instructions, "Set result.decision as a string value derived from llm_query execution.");
        assertInstructionExists(instructions, "Set result.reason as a string value derived from llm_query execution.");
        assertInstructionExists(instructions, "Set result.code as a string value derived from llm_query execution.");

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
}