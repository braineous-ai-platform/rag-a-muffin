package ai.braineous.cgo.scorer;

import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScorerTests {
    @Test
    void calculateScore_allValidationsOk_shouldReturnOnePointZero() {
        Scorer scorer = new Scorer();

        // ____Arrange____
        ValidationResult prompt = ValidationResult.ok();
        ValidationResult llm = ValidationResult.ok();
        ValidationResult domain = ValidationResult.ok();

        QueryExecution execution = new QueryExecution(
                /* request */ null,
                /* parsedResponse */ null,
                prompt,
                llm,
                domain
        );

        ScorerContext ctx = new ScorerContext(execution);

        // ____Act____
        Console.log("ScorerTest_HappyPath_START", execution);
        ScorerResult result = scorer.calculateScore(ctx);
        Console.log("ScorerResult", result);

        // ____Assert____
        assertNotNull(result);
        assertEquals(1.0d, result.getScore(), 0.0001);

        // Optional: ensure history got a record (conceptually)
        // assertTrue(store.wasLastRecord(execution, result));
    }

    @Test
    void calculateScore_promptValidationError_shouldReturnZeroPointZero() {
        Scorer scorer = new Scorer();

        // ____Arrange____
        ValidationResult prompt = ValidationResult.error(
                "PROMPT_INVALID",
                "Prompt validation failed"
        );
        ValidationResult llm = ValidationResult.ok();
        ValidationResult domain = ValidationResult.ok();

        QueryExecution execution = new QueryExecution(
                /* request */ null,
                /* parsedResponse */ null,
                prompt,
                llm,
                domain
        );

        ScorerContext ctx = new ScorerContext(execution);

        // ____Act____
        Console.log("ScorerTest_PromptError_START", execution);
        ScorerResult result = scorer.calculateScore(ctx);
        Console.log("ScorerResult", result);

        // ____Assert____
        assertNotNull(result);
        assertEquals(0.0d, result.getScore(), 0.0001);
    }

    @Test
    void calculateScore_llmValidationError_shouldReturnZeroPointThreeThree() {
        Scorer scorer = new Scorer();

        // ____Arrange____
        ValidationResult prompt = ValidationResult.ok();
        ValidationResult llm = ValidationResult.error(
                "LLM_CONTRACT_VIOLATION",
                "LLM response did not match schema"
        );
        ValidationResult domain = ValidationResult.ok();

        QueryExecution execution = new QueryExecution(
                /* request */ null,
                /* parsedResponse */ null,
                prompt,
                llm,
                domain
        );

        ScorerContext ctx = new ScorerContext(execution);

        // ____Act____
        Console.log("ScorerTest_LLMError_START", execution);
        ScorerResult result = scorer.calculateScore(ctx);
        Console.log("ScorerResult", result);

        // ____Assert____
        assertNotNull(result);
        assertEquals(0.33d, result.getScore(), 0.0001);
    }

    @Test
    void calculateScore_domainValidationError_shouldReturnZeroPointSixSix() {
        Scorer scorer = new Scorer();

        // ____Arrange____
        ValidationResult prompt = ValidationResult.ok();
        ValidationResult llm = ValidationResult.ok();
        ValidationResult domain = ValidationResult.error(
                "DOMAIN_RULE_FAILED",
                "Domain validation failed"
        );

        QueryExecution execution = new QueryExecution(
                /* request */ null,
                /* parsedResponse */ null,
                prompt,
                llm,
                domain
        );

        ScorerContext ctx = new ScorerContext(execution);

        // ____Act____
        Console.log("ScorerTest_DomainError_START", execution);
        ScorerResult result = scorer.calculateScore(ctx);
        Console.log("ScorerResult", result);

        // ____Assert____
        assertNotNull(result);
        assertEquals(0.66d, result.getScore(), 0.0001);
    }

    @Test
    void calculateScore_allValidationsError_shouldStillReturnZeroPointZero_dueToPromptDominance() {
        Scorer scorer = new Scorer();

        // ____Arrange____
        ValidationResult prompt = ValidationResult.error(
                "PROMPT_INVALID",
                "Prompt validation failed"
        );
        ValidationResult llm = ValidationResult.error(
                "LLM_CONTRACT_VIOLATION",
                "LLM response did not match schema"
        );
        ValidationResult domain = ValidationResult.error(
                "DOMAIN_RULE_FAILED",
                "Domain validation failed"
        );

        QueryExecution execution = new QueryExecution(
                /* request */ null,
                /* parsedResponse */ null,
                prompt,
                llm,
                domain
        );

        ScorerContext ctx = new ScorerContext(execution);

        // ____Act____
        Console.log("ScorerTest_AllError_START", execution);
        ScorerResult result = scorer.calculateScore(ctx);
        Console.log("ScorerResult", result);

        // ____Assert____
        assertNotNull(result);
        assertEquals(0.0d, result.getScore(), 0.0001);
    }

    @Test
    void calculateScore_anyValidationNull_shouldReturnZeroPointZero() {
        Scorer scorer = new Scorer();

        // ____Arrange____
        ValidationResult prompt = null;            // simulate missing validation
        ValidationResult llm = ValidationResult.ok();
        ValidationResult domain = ValidationResult.ok();

        QueryExecution execution = new QueryExecution(
                /* request */ null,
                /* parsedResponse */ null,
                prompt,
                llm,
                domain
        );

        ScorerContext ctx = new ScorerContext(execution);

        // ____Act____
        Console.log("ScorerTest_NullGuard_START", execution);
        ScorerResult result = scorer.calculateScore(ctx);
        Console.log("ScorerResult", result);

        // ____Assert____
        assertNotNull(result);
        assertEquals(0.0d, result.getScore(), 0.0001);
    }

    @Test
    void calculateScore_nullContextOrNullExecution_shouldReturnNullSilently() {
        Scorer scorer = new Scorer();

        // ____Arrange____
        ScorerContext nullContext = null;

        // ____Act____
        Console.log("ScorerTest_NullContext_START", nullContext);
        ScorerResult result1 = scorer.calculateScore(nullContext);
        Console.log("ScorerResult_NullContext", result1);

        // ____Assert____
        assertNull(result1);

        // ---- second branch: context exists but execution null ----
        ScorerContext ctxWithNullExecution = new ScorerContext(null); // execution = null inside

        Console.log("ScorerTest_NullExecution_START", ctxWithNullExecution);
        ScorerResult result2 = scorer.calculateScore(ctxWithNullExecution);
        Console.log("ScorerResult_NullExecution", result2);

        assertNull(result2);
    }
}
