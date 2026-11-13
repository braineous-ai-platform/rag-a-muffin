package ai.braineous.cgo.nexus.template;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateGeneratorTest {

    @Test
    public void test_1() {
        String actual = new TemplateGenerator().generate(
                "inventory availability.",
                BigDecimal.ONE);
        System.out.print("=== test_1 ===\n");
        System.out.print(actual);
        System.out.print("\n");
        assertEquals(
                "inventory availability.\n\ncontext\nThe result is 1.",
                actual);
        assertTrue(actual.startsWith("inventory availability."));
    }

    @Test
    public void test_2() {
        String actual = new TemplateGenerator().generate(
                "status check.",
                new BigDecimal("20"));
        System.out.print("=== test_2 ===\n");
        System.out.print(actual);
        System.out.print("\n");
        assertTrue(actual.startsWith("status check."));
        assertTrue(actual.contains("The result is 20."));
        assertFalse(actual.startsWith("inventory availability."));
    }

    @Test
    public void test_3() {
        String actual = new TemplateGenerator().generate(
                "alpha.",
                new BigDecimal("1E-33"));
        System.out.print("=== test_3 ===\n");
        System.out.print(actual);
        System.out.print("\n");
        assertTrue(actual.contains("The result is 0.000000000000000000000000000000001."));
    }

    @Test
    public void test_4() {
        String actual = new TemplateGenerator().generate(
                "alpha.",
                BigDecimal.ZERO);
        System.out.print("=== test_4 ===\n");
        System.out.print(actual);
        System.out.print("\n");
        assertTrue(actual.contains("\ncontext\n"));
        assertFalse(actual.contains("\nrule\n"));
        assertFalse(actual.contains("\ndecision\n"));
        assertFalse(actual.contains("llm_instructions"));
    }

    @Test
    public void test_5() {
        String actual = new TemplateGenerator().generate(
                "alpha.",
                BigDecimal.ZERO);
        String[] blocks = actual.split("\n\n");
        System.out.print("=== test_5 ===\n");
        System.out.print(String.valueOf(blocks.length));
        System.out.print("\n");
        assertEquals(2, blocks.length);
        assertEquals("alpha.", blocks[0]);
        assertEquals("context\nThe result is 0.", blocks[1]);
    }

    @Test
    public void test_6() {
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("account", "emp-042");
        values.put("location", "germany");
        String actual = new TemplateGenerator().generate(
                "security investigation",
                values);
        System.out.print("=== test_6 ===\n");
        System.out.print(actual);
        System.out.print("\n");
        assertEquals(
                "security investigation\n\ncontext\n"
                        + "account: emp-042\n"
                        + "location: germany",
                actual);
        assertTrue(actual.startsWith("security investigation"));
        assertFalse(actual.contains("\nrule\n"));
        assertFalse(actual.contains("\ndecision\n"));
        assertFalse(actual.contains("The result is"));
    }

    @Test
    public void test_7() {
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("location", "germany");
        values.put("account", "emp-042");
        String actual = new TemplateGenerator().generate(
                "security investigation",
                values);
        System.out.print("=== test_7 ===\n");
        System.out.print(actual);
        System.out.print("\n");
        int locationIndex = actual.indexOf("location: germany");
        int accountIndex = actual.indexOf("account: emp-042");
        assertTrue(locationIndex >= 0);
        assertTrue(accountIndex >= 0);
        assertTrue(locationIndex < accountIndex);
    }
}
