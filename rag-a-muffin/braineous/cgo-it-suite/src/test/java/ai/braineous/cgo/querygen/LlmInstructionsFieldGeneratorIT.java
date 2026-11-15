package ai.braineous.cgo.querygen;

import ai.braineous.rag.prompt.cgo.querygen.LlmInstructionsFieldGenerator;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LlmInstructionsFieldGeneratorIT {

    @Test
    public void test_1() {
        String actual = new LlmInstructionsFieldGenerator().generateNexusInstructions();

        Console.log("llm.instructions.nexus.it", actual);

        String expected =
                "This is the prompt.\n\n"
                        + "Context provides the facts.\n"
                        + "Rule defines the conditions.\n"
                        + "Decision defines the question.\n"
                        + "The response is the answer.";

        assertEquals(expected, actual);
        assertFalse(actual.contains("task.controls"));
        assertFalse(actual.contains("execute a function"));
        assertFalse(actual.contains("llm_instructions"));
        assertFalse(actual.contains("output_template"));
    }
}
