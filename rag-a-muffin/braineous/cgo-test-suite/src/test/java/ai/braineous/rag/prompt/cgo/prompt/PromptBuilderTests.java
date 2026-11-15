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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
                loadV14Query();

        PromptRequestOutput output =
                builder.generateExecutionPrompt(llmQuery);

        JsonObject result =
                output.getRequestOutput();

        String prompt =
                result.get("prompt").getAsString();

        Console.log("execution.prompt", prompt);

        Assertions.assertNotNull(output);
        Assertions.assertNotNull(result);

        Assertions.assertEquals(llmQuery.toString(), prompt);

        JsonObject parsedPrompt =
                JsonParser.parseString(prompt).getAsJsonObject();

        Assertions.assertEquals(llmQuery, parsedPrompt);

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

        Assertions.assertEquals("{}", prompt);
        Assertions.assertTrue(output.getValidationResult().isOk());
    }

    @Test
    public void test_8() {
        PromptBuilder builder =
                new PromptBuilder();

        JsonObject llmQuery =
                loadV14Query();

        PromptRequestOutput output =
                builder.generateExecutionPrompt(llmQuery);

        String prompt =
                output.getRequestOutput()
                        .get("prompt")
                        .getAsString();

        Console.log("execution.prompt.order", prompt);

        Assertions.assertEquals(llmQuery.toString(), prompt);

        JsonObject parsedPrompt =
                JsonParser.parseString(prompt).getAsJsonObject();

        Assertions.assertEquals(llmQuery, parsedPrompt);
        Assertions.assertEquals(3, parsedPrompt.size());
        Assertions.assertTrue(
                prompt.indexOf("\"llm_instructions\"") < prompt.indexOf("\"task\"")
        );
        Assertions.assertTrue(
                prompt.indexOf("\"task\"") < prompt.indexOf("\"context\"")
        );
    }

    @Test
    public void test_9() {
        PromptBuilder builder = new PromptBuilder();
        String nexus =
                "inventory availability.\n\ncontext\nThe result is 0.";
        String rule =
                "rule\n"
                        + "if the result is greater than or equal to 1,\n"
                        + "the inventory is available.\n"
                        + "otherwise, the inventory is unavailable.";
        String decision =
                "decision\n"
                        + "state the inventory status as either available or unavailable.";

        PromptRequestOutput output =
                builder.generateExecutionPrompt(nexus, rule, decision);
        String prompt =
                output.getRequestOutput().get("prompt").getAsString();

        Console.log("execution.prompt.nexus", prompt);

        Assertions.assertEquals(
                nexus + "\n\n" + rule + "\n\n" + decision,
                prompt);
        Assertions.assertTrue(prompt.startsWith(nexus));
        Assertions.assertTrue(prompt.indexOf(nexus) < prompt.indexOf(rule));
        Assertions.assertTrue(prompt.indexOf(rule) < prompt.indexOf(decision));
        Assertions.assertEquals(
                "prompt.execution.ok",
                output.getValidationResult().getCode());
        Assertions.assertFalse(prompt.contains("llm_instructions"));
        Assertions.assertFalse(prompt.contains("task.controls"));
    }

    @Test
    public void test_10() {
        String nexus =
                "security investigation\n\ncontext\n"
                        + "account: Account:emp-042\n"
                        + "location: germany\n"
                        + "device: macbook pro\n"
                        + "failedLoginAttempts: 3\n"
                        + "successfulLoginAttempts: 1";
        String rule =
                "rule\n"
                        + "when multiple failed login attempts are followed by\n"
                        + "a successful login attempt,\n"
                        + "the activity warrants a security investigation.\n"
                        + "otherwise, routine monitoring is sufficient.";
        String decision =
                "decision\n"
                        + "based on the context and the rule,\n"
                        + "does this account activity warrant a security investigation\n"
                        + "or routine monitoring?";

        String prompt =
                new PromptBuilder()
                        .generateExecutionPrompt(nexus, rule, decision)
                        .getRequestOutput()
                        .get("prompt")
                        .getAsString();

        Console.log("execution.prompt.nexus.verbatim", prompt);

        Assertions.assertEquals(
                "security investigation\n\ncontext\n"
                        + "account: Account:emp-042\n"
                        + "location: germany\n"
                        + "device: macbook pro\n"
                        + "failedLoginAttempts: 3\n"
                        + "successfulLoginAttempts: 1\n\n"
                        + rule
                        + "\n\n"
                        + decision,
                prompt);
        Assertions.assertTrue(prompt.contains("account: Account:emp-042"));
        Assertions.assertTrue(prompt.contains("failedLoginAttempts: 3"));
        Assertions.assertFalse(prompt.contains("Failed Login"));
        Assertions.assertEquals(
                prompt.indexOf("\n\nrule\n"),
                nexus.length());
        Assertions.assertEquals(
                prompt.indexOf("\n\ndecision\n"),
                nexus.length() + 2 + rule.length());
    }

    @Test
    public void test_11() {
        PromptBuilder builder = new PromptBuilder();

        String allNull =
                builder.generateExecutionPrompt(null, null, null)
                        .getRequestOutput()
                        .get("prompt")
                        .getAsString();
        Console.log("execution.prompt.nexus.null.all", allNull);
        Assertions.assertEquals("\n\n\n\n", allNull);

        String nexusOnly =
                builder.generateExecutionPrompt(
                                "inventory availability.\n\ncontext\nThe result is 0.",
                                null,
                                null)
                        .getRequestOutput()
                        .get("prompt")
                        .getAsString();
        Console.log("execution.prompt.nexus.null.rule.decision", nexusOnly);
        Assertions.assertEquals(
                "inventory availability.\n\ncontext\nThe result is 0.\n\n\n\n",
                nexusOnly);

        String ruleAndDecision =
                builder.generateExecutionPrompt(
                                null,
                                "rule\nbody",
                                "decision\nbody")
                        .getRequestOutput()
                        .get("prompt")
                        .getAsString();
        Console.log("execution.prompt.nexus.null.title", ruleAndDecision);
        Assertions.assertEquals(
                "\n\nrule\nbody\n\ndecision\nbody",
                ruleAndDecision);
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

    private JsonObject loadV14Query() {
        InputStream stream =
                PromptBuilderTests.class.getResourceAsStream(
                        "/ai/braineous/rag/prompt/cgo/prompt/v14-query.json"
                );

        Assertions.assertNotNull(stream);

        return JsonParser.parseReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        ).getAsJsonObject();
    }
}