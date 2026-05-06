package ai.braineous.rag.prompt.cgo.prompt;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.GsonPromptRequestValidator;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class PromptBuilderTests {



    @Test
    public void test_1() {
        PromptBuilder builder =
                new PromptBuilder(
                        new SimpleResponseContractRegistry()
                );

        QueryRequest<ValidateTask> request =
                buildRequest();

        PromptRequestOutput output =
                builder.generateRequestPrompt(request);

        JsonObject root =
                output.getRequestOutput();

        Console.log("prompt.request.root", root.toString());

        Assertions.assertNotNull(output);
        Assertions.assertNotNull(root);

        Assertions.assertTrue(root.has("meta"));
        Assertions.assertTrue(root.has("context"));
        Assertions.assertTrue(root.has("task"));
        Assertions.assertTrue(root.has("response_contract"));
        Assertions.assertTrue(root.has("instructions"));
        Assertions.assertTrue(root.has("llm_instructions"));

        JsonObject meta =
                root.getAsJsonObject("meta");

        Assertions.assertEquals("v1", meta.get("version").getAsString());
        Assertions.assertEquals("decision", meta.get("query_kind").getAsString());

        JsonObject context =
                root.getAsJsonObject("context");

        Assertions.assertTrue(context.has("nodes"));

        JsonObject task =
                root.getAsJsonObject("task");

        Assertions.assertEquals(
                "PaymentRequest:PAY-1001",
                task.get("factId").getAsString()
        );

        JsonArray instructions =
                root.getAsJsonArray("instructions");

        Assertions.assertEquals(3, instructions.size());

        JsonArray llmInstructions =
                root.getAsJsonArray("llm_instructions");

        Assertions.assertNotNull(llmInstructions);

        JsonObject responseContract =
                root.getAsJsonObject("response_contract");

        Assertions.assertEquals(
                "unknown",
                responseContract.get("type").getAsString()
        );
    }

    @Test
    public void test_2() {
        final String[] captured =
                new String[1];

        PhaseResultValidator validator = raw -> {

            captured[0] = raw;

            return ValidationResult.createInternal(
                    true,
                    "prompt.contract.ok",
                    "Prompt contract valid",
                    "prompt_contract_validation",
                    null,
                    Collections.<String, Object>emptyMap()
            );
        };

        PromptBuilder builder =
                new PromptBuilder(
                        new SimpleResponseContractRegistry(),
                        validator
                );

        PromptRequestOutput output =
                builder.generateRequestPrompt(buildRequest());

        Console.log("prompt.validation.ok", String.valueOf(output.getValidationResult()));
        Console.log("prompt.validation.captured", captured[0]);

        Assertions.assertNotNull(output.getValidationResult());
        Assertions.assertTrue(output.getValidationResult().isOk());
        Assertions.assertEquals(
                "prompt.contract.ok",
                output.getValidationResult().getCode()
        );

        Assertions.assertNotNull(captured[0]);
        Assertions.assertFalse(captured[0].isEmpty());
    }

    @Test
    public void test_3() {
        PhaseResultValidator validator = raw ->
                ValidationResult.createInternal(
                        false,
                        "prompt.contract.error",
                        "Prompt invalid",
                        "prompt_contract_validation",
                        null,
                        Collections.<String, Object>emptyMap()
                );

        PromptBuilder builder =
                new PromptBuilder(
                        new SimpleResponseContractRegistry(),
                        validator
                );

        PromptRequestOutput output =
                builder.generateRequestPrompt(buildRequest());

        Console.log("prompt.validation.error", String.valueOf(output.getValidationResult()));

        Assertions.assertNotNull(output.getValidationResult());
        Assertions.assertFalse(output.getValidationResult().isOk());
        Assertions.assertEquals(
                "prompt.contract.error",
                output.getValidationResult().getCode()
        );
    }

    @Test
    public void test_4() {
        GsonPromptRequestValidator realValidator =
                new GsonPromptRequestValidator();

        PromptBuilder builder =
                new PromptBuilder(
                        new SimpleResponseContractRegistry(),
                        realValidator::validate
                );

        PromptRequestOutput output =
                builder.generateRequestPrompt(buildRequest());

        Console.log("prompt.real.validator", String.valueOf(output.getValidationResult()));

        Assertions.assertNotNull(output.getValidationResult());
        Assertions.assertTrue(output.getValidationResult().isOk());
        Assertions.assertEquals(
                "prompt.contract.ok",
                output.getValidationResult().getCode()
        );
    }

    @Test
    public void test_5() {
        GsonPromptRequestValidator realValidator =
                new GsonPromptRequestValidator();

        PhaseResultValidator tamperingValidator = raw -> {

            JsonObject object =
                    JsonParser.parseString(raw).getAsJsonObject();

            object.remove("meta");

            return realValidator.validate(object.toString());
        };

        PromptBuilder builder =
                new PromptBuilder(
                        new SimpleResponseContractRegistry(),
                        tamperingValidator
                );

        PromptRequestOutput output =
                builder.generateRequestPrompt(buildRequest());

        Console.log("prompt.real.validator.tampered",
                String.valueOf(output.getValidationResult()));

        Assertions.assertNotNull(output.getValidationResult());
        Assertions.assertFalse(output.getValidationResult().isOk());
        Assertions.assertEquals(
                "prompt.contract.meta_missing_or_invalid",
                output.getValidationResult().getCode()
        );
    }

    @Test
    public void test_6() {
        PromptBuilder builder =
                new PromptBuilder();

        JsonObject llmQuery =
                buildExecutionQuery();

        PromptRequestOutput output =
                builder.generateExecutionPrompt(llmQuery);

        JsonObject result =
                output.getRequestOutput();

        String prompt =
                result.get("prompt").getAsString();

        Console.log("execution.prompt", prompt);

        Assertions.assertNotNull(output);
        Assertions.assertNotNull(result);

        Assertions.assertTrue(prompt.contains(
                "You are an execution engine, not a document reader."
        ));

        Assertions.assertTrue(prompt.contains(
                "Return only JSON."
        ));

        Assertions.assertTrue(prompt.contains("INPUT:"));

        Assertions.assertTrue(prompt.contains("\"task\""));
        Assertions.assertTrue(prompt.contains("\"context\""));
        Assertions.assertTrue(prompt.contains("\"output_template\""));

        Assertions.assertFalse(prompt.contains("\"llm_instructions\""));

        Assertions.assertNotNull(output.getValidationResult());
        Assertions.assertTrue(output.getValidationResult().isOk());
        Assertions.assertEquals(
                "prompt.execution.ok",
                output.getValidationResult().getCode()
        );
    }

    @Test
    public void test_7() {
        PromptBuilder builder =
                new PromptBuilder();

        PromptRequestOutput output =
                builder.generateExecutionPrompt(new JsonObject());

        String prompt =
                output.getRequestOutput()
                        .get("prompt")
                        .getAsString();

        Console.log("execution.prompt.empty", prompt);

        Assertions.assertTrue(prompt.contains("INPUT:"));
        Assertions.assertFalse(prompt.contains("\"task\""));
        Assertions.assertFalse(prompt.contains("\"context\""));
        Assertions.assertFalse(prompt.contains("\"output_template\""));

        Assertions.assertTrue(output.getValidationResult().isOk());
    }

    @Test
    public void test_8() {
        PromptBuilder builder =
                new PromptBuilder();

        JsonObject llmQuery =
                buildExecutionQuery();

        PromptRequestOutput output =
                builder.generateExecutionPrompt(llmQuery);

        String prompt =
                output.getRequestOutput()
                        .get("prompt")
                        .getAsString();

        Console.log("execution.prompt.order", prompt);

        int instructionsIndex =
                prompt.indexOf("You are an execution engine");

        int inputIndex =
                prompt.indexOf("INPUT:");

        int taskIndex =
                prompt.indexOf("\"task\"");

        Assertions.assertTrue(instructionsIndex >= 0);
        Assertions.assertTrue(inputIndex > instructionsIndex);
        Assertions.assertTrue(taskIndex > inputIndex);
    }

    private QueryRequest<ValidateTask> buildRequest() {

        Meta meta =
                new Meta(
                        "v1",
                        "decision",
                        "pay decision"
                );

        ValidateTask task =
                new ValidateTask(
                        "pay decision",
                        "PaymentRequest:PAY-1001",
                        Arrays.asList("decision", "reason", "code"),
                        Arrays.asList(
                                "CustomerAccount:CUST-2001"
                        )
                );

        Node payment =
                new Node(
                        "PaymentRequest:PAY-1001",
                        "{\"amount\":\"125.00\"}",
                        Collections.<String>emptyList(),
                        Node.Mode.ATOMIC
                );

        Map<String, Node> nodes =
                new HashMap<String, Node>();

        nodes.put(payment.getId(), payment);

        GraphContext context =
                new GraphContext(nodes);

        return new QueryRequest<ValidateTask>(
                meta,
                context,
                task
        );
    }

    private JsonObject buildExecutionQuery() {

        JsonObject root =
                new JsonObject();

        JsonObject llmInstructions =
                new JsonObject();

        JsonArray instructions =
                new JsonArray();

        instructions.add("You are an execution engine, not a document reader.");
        instructions.add("Return only JSON.");

        llmInstructions.add("instructions", instructions);

        JsonObject task =
                new JsonObject();

        task.addProperty("factId", "PaymentRequest:PAY-1001");

        JsonObject context =
                new JsonObject();

        JsonObject outputTemplate =
                new JsonObject();

        JsonObject result =
                new JsonObject();

        result.addProperty("decision", "");
        result.addProperty("reason", "");
        result.addProperty("code", "");

        outputTemplate.add("result", result);

        root.add("llm_instructions", llmInstructions);
        root.add("task", task);
        root.add("context", context);
        root.add("output_template", outputTemplate);

        return root;
    }
}