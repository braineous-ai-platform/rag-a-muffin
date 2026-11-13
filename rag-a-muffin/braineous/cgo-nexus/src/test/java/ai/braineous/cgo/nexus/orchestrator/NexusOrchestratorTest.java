package ai.braineous.cgo.nexus.orchestrator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NexusOrchestratorTest {

    @Test
    public void test_1() {
        String actual = new NexusOrchestrator().execute(
                null,
                List.of(),
                "1",
                "inventory availability.");
        System.out.print("=== test_1 ===\n");
        System.out.print(actual);
        System.out.print("\n");
        assertEquals(
                "inventory availability.\n\ncontext\nThe result is 1.",
                actual);
        assertFalse(actual.contains("account:"));
    }

    @Test
    public void test_2() {
        String actual = new NexusOrchestrator().execute(
                null,
                List.of(),
                null,
                "security investigation");
        System.out.print("=== test_2 ===\n");
        System.out.print(actual);
        System.out.print("\n");
        assertEquals("security investigation\n\ncontext", actual);
        assertFalse(actual.contains("The result is"));
        assertTrue(actual.startsWith("security investigation"));
    }

    @Test
    public void test_3() {
        String actual = new NexusOrchestrator().execute(
                null,
                List.of(),
                "   ",
                "security investigation");
        System.out.print("=== test_3 ===\n");
        System.out.print(actual);
        System.out.print("\n");
        assertEquals("security investigation\n\ncontext", actual);
        assertFalse(actual.contains("The result is"));
    }
}
