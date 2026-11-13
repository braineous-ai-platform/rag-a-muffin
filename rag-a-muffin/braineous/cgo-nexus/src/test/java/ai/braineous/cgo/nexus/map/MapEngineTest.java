package ai.braineous.cgo.nexus.map;

import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MapEngineTest {

    @Test
    public void test_1() {
        Map<String, String> actual = new MapEngine().evaluate(
                this.resolved(
                        this.fact(
                                "Account:emp-042",
                                "{\"id\":\"Account:emp-042\",\"kind\":\"Account\","
                                        + "\"mode\":\"atomic\",\"account\":\"emp-042\"}"),
                        this.fact(
                                "Location:germany",
                                "{\"id\":\"Location:germany\",\"kind\":\"Location\","
                                        + "\"mode\":\"atomic\",\"location\":\"germany\"}")));
        System.out.print("=== test_1 ===\n");
        System.out.print(String.valueOf(actual));
        System.out.print("\n");
        assertEquals(2, actual.size());
        assertEquals("emp-042", actual.get("account"));
        assertEquals("germany", actual.get("location"));
    }

    @Test
    public void test_2() {
        Map<String, String> actual = new MapEngine().evaluate(
                this.resolved(
                        this.fact(
                                "FailedLogin:FL-001",
                                "{\"id\":\"FailedLogin:FL-001\",\"kind\":\"FailedLogin\","
                                        + "\"mode\":\"atomic\",\"failedLoginAttempts\":3,"
                                        + "\"label\":\"macbook pro\",\"active\":true}")));
        System.out.print("=== test_2 ===\n");
        System.out.print(String.valueOf(actual));
        System.out.print("\n");
        assertEquals("3", actual.get("failedLoginAttempts"));
        assertEquals("macbook pro", actual.get("label"));
        assertEquals("true", actual.get("active"));
    }

    @Test
    public void test_3() {
        Map<String, String> actual = new MapEngine().evaluate(
                this.resolved(
                        this.fact(
                                "Account:emp-042",
                                "{\"id\":\"Account:emp-042\",\"kind\":\"Account\","
                                        + "\"mode\":\"atomic\",\"account\":\"emp-042\"}")));
        System.out.print("=== test_3 ===\n");
        System.out.print(String.valueOf(actual));
        System.out.print("\n");
        assertFalse(actual.containsKey("id"));
        assertFalse(actual.containsKey("kind"));
        assertFalse(actual.containsKey("mode"));
        assertEquals("emp-042", actual.get("account"));
    }

    @Test
    public void test_4() {
        Map<String, String> actual = new MapEngine().evaluate(
                this.resolved(
                        this.fact(
                                "Account:emp-042",
                                "{\"id\":\"Account:emp-042\",\"kind\":\"Account\","
                                        + "\"mode\":\"atomic\",\"account\":\"emp-042\","
                                        + "\"profile\":{\"city\":\"berlin\"},"
                                        + "\"tags\":[\"a\"],\"note\":null}")));
        System.out.print("=== test_4 ===\n");
        System.out.print(String.valueOf(actual));
        System.out.print("\n");
        assertFalse(actual.containsKey("profile"));
        assertFalse(actual.containsKey("tags"));
        assertFalse(actual.containsKey("note"));
        assertEquals(1, actual.size());
        assertEquals("emp-042", actual.get("account"));
    }

    @Test
    public void test_5() {
        Map<String, String> actual = new MapEngine().evaluate(
                this.resolved(
                        this.fact(
                                "Account:emp-042",
                                "{\"id\":\"Account:emp-042\",\"kind\":\"Account\","
                                        + "\"mode\":\"atomic\",\"account\":\"emp-042\"}"),
                        this.fact(
                                "Location:germany",
                                "{\"id\":\"Location:germany\",\"kind\":\"Location\","
                                        + "\"mode\":\"atomic\",\"location\":\"germany\"}"),
                        this.fact(
                                "Device:macbook-pro",
                                "{\"id\":\"Device:macbook-pro\",\"kind\":\"Device\","
                                        + "\"mode\":\"atomic\",\"device\":\"macbook pro\"}"),
                        this.fact(
                                "FailedLogin:FL-001",
                                "{\"id\":\"FailedLogin:FL-001\",\"kind\":\"FailedLogin\","
                                        + "\"mode\":\"atomic\",\"failedLoginAttempts\":3}"),
                        this.fact(
                                "SuccessfulLogin:SL-001",
                                "{\"id\":\"SuccessfulLogin:SL-001\","
                                        + "\"kind\":\"SuccessfulLogin\",\"mode\":\"atomic\","
                                        + "\"successfulLoginAttempts\":1}")));
        System.out.print("=== test_5 ===\n");
        System.out.print(String.valueOf(actual));
        System.out.print("\n");
        assertEquals("emp-042", actual.get("account"));
        assertEquals("germany", actual.get("location"));
        assertEquals("macbook pro", actual.get("device"));
        assertEquals("3", actual.get("failedLoginAttempts"));
        assertEquals("1", actual.get("successfulLoginAttempts"));
        assertEquals(5, actual.size());
    }

    private Fact fact(String id, String text) {
        Fact fact = new Fact(id, text);
        fact.setMode("atomic");
        return fact;
    }

    private ResolvedGraph resolved(Fact... facts) {
        Map<String, Fact> nodes = new LinkedHashMap<String, Fact>();
        int index = 0;
        while (index < facts.length) {
            Fact fact = facts[index];
            nodes.put(fact.getId(), fact);
            index = index + 1;
        }
        GraphSnapshot snapshot = new GraphSnapshot(
                nodes,
                new LinkedHashMap<String, Edge>());
        return new ResolvedGraph(facts[0], snapshot);
    }
}
