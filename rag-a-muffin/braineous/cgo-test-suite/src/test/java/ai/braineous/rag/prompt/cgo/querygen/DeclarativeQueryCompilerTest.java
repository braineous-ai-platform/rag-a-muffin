package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeclarativeQueryCompilerTest {

    @Test
    public void test_1() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "select from llm "
                + "where factId = 'Flight:F100' "
                + "and relatedFactIds = 'Airport:AUS,Airport:DFW' "
                + "and expression = 'departure_code is present AND arrival_code is present' "
                + "and definition = 'Flight Airport Validation'";

        JsonObject json = compiler.compile(sql);

        Console.log("test_1_json", json.toString());

        JsonObject task = json.getAsJsonObject("task");
        JsonObject intent = task.getAsJsonObject("intent");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
        JsonArray select = task.getAsJsonArray("select");

        Assertions.assertEquals(0, intent.size());
        Assertions.assertEquals("Flight:F100", task.get("factId").getAsString());

        Assertions.assertEquals(2, relatedFactIds.size());
        Assertions.assertEquals("Airport:AUS", relatedFactIds.get(0).getAsString());
        Assertions.assertEquals("Airport:DFW", relatedFactIds.get(1).getAsString());

        Assertions.assertEquals("departure_code is present AND arrival_code is present", task.get("expression").getAsString());
        Assertions.assertEquals("Flight Airport Validation", task.get("definition").getAsString());

        Assertions.assertEquals(0, select.size());
        Assertions.assertEquals(
                java.util.Set.of("intent", "factId", "relatedFactIds", "expression", "definition", "select"),
                task.keySet()
        );
        Assertions.assertFalse(task.has("constraints"));
        Assertions.assertFalse(task.has("control"));
        Assertions.assertFalse(task.has("parameters"));
        Assertions.assertFalse(task.has("spec"));
        Assertions.assertFalse(task.has("result"));
        Assertions.assertFalse(task.has("rule"));
        Assertions.assertFalse(task.has("decision"));
        Assertions.assertFalse(task.has("ruleSection"));
        Assertions.assertFalse(task.has("decisionSection"));
    }

    @Test
    public void test_2() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "select from llm "
                + "where factId = 'Flight:F100' "
                + "and relatedFactIds = 'Airport:AUS, Airport:DFW' "
                + "and definition = 'Flight Airport Validation'";

        JsonObject json = compiler.compile(sql);

        Console.log("test_2_json", json.toString());

        JsonObject task = json.getAsJsonObject("task");
        JsonObject intent = task.getAsJsonObject("intent");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
        JsonArray select = task.getAsJsonArray("select");

        Assertions.assertEquals(0, intent.size());
        Assertions.assertEquals("Flight:F100", task.get("factId").getAsString());

        Assertions.assertEquals(2, relatedFactIds.size());
        Assertions.assertEquals("Airport:AUS", relatedFactIds.get(0).getAsString());
        Assertions.assertEquals("Airport:DFW", relatedFactIds.get(1).getAsString());

        Assertions.assertEquals("", task.get("expression").getAsString());
        Assertions.assertEquals("Flight Airport Validation", task.get("definition").getAsString());
        Assertions.assertEquals(0, select.size());
        Assertions.assertFalse(task.has("constraints"));
    }

    @Test
    public void test_3() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "select from llm "
                + "where relatedFactIds = 'Airport:AUS,Airport:DFW' "
                + "and definition = 'Flight Airport Validation'";

        try {
            compiler.compile(sql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_3_exception", e.getMessage());
            Assertions.assertEquals("factId must be provided", e.getMessage());
        }
    }

    @Test
    public void test_4() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "from llm "
                + "where factId = 'Flight:F100' "
                + "and relatedFactIds = 'Airport:AUS,Airport:DFW' "
                + "and definition = 'Flight Airport Validation'";

        try {
            compiler.compile(sql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_4_exception", e.getMessage());
            Assertions.assertEquals("missing select", e.getMessage());
        }
    }

    @Test
    public void test_5() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "select from llm "
                + "where factId = 'Flight:F100' "
                + "and relatedFactIds = 'Airport:AUS, Airport:DFW, Airport:JFK' "
                + "and definition = 'Flight Airport Validation'";

        JsonObject json = compiler.compile(sql);

        Console.log("test_5_json", json.toString());

        JsonArray relatedFactIds = json
                .getAsJsonObject("task")
                .getAsJsonArray("relatedFactIds");

        Assertions.assertEquals(3, relatedFactIds.size());
        Assertions.assertEquals("Airport:AUS", relatedFactIds.get(0).getAsString());
        Assertions.assertEquals("Airport:DFW", relatedFactIds.get(1).getAsString());
        Assertions.assertEquals("Airport:JFK", relatedFactIds.get(2).getAsString());
    }

    @Test
    public void test_6() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = "\n"
                + "select from llm\n"
                + "where factId = 'Flight:F100'\n"
                + "and relatedFactIds = 'Airport:AUS,Airport:DFW'\n"
                + "and expression = 'departure_code != arrival_code'\n"
                + "and definition = 'Flight Airport Validation'\n";

        JsonObject json = compiler.compile(sql);

        Console.log("test_6_json", json.toString());

        JsonObject task = json.getAsJsonObject("task");

        Assertions.assertEquals("Flight:F100", task.get("factId").getAsString());
        Assertions.assertEquals(
                0,
                task.getAsJsonObject("intent").size()
        );
        Assertions.assertEquals("departure_code != arrival_code",
                task.get("expression").getAsString());
        Assertions.assertEquals("Flight Airport Validation",
                task.get("definition").getAsString());
        Assertions.assertFalse(task.has("constraints"));
    }

    @Test
    public void test_7() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        try {
            compiler.compile(null);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_7_exception", e.getMessage());
            Assertions.assertEquals("sql must not be null", e.getMessage());
        }
    }

    @Test
    public void test_8() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "select from llm "
                + "where factId = 'Flight:F100' "
                + "and definition = 'Flight Airport Validation'";

        JsonObject json = compiler.compile(sql);

        Console.log("test_8_json", json.toString());

        JsonObject task = json.getAsJsonObject("task");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");

        Assertions.assertEquals("Flight:F100", task.get("factId").getAsString());
        Assertions.assertEquals(0, relatedFactIds.size());
        Assertions.assertEquals("", task.get("expression").getAsString());
        Assertions.assertEquals("Flight Airport Validation", task.get("definition").getAsString());
    }

    @Test
    public void test_9() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "select from llm "
                + "where factId = 'Payment:P100'";

        try {
            compiler.compile(sql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_9_exception", e.getMessage());
            Assertions.assertEquals("definition must be provided", e.getMessage());
        }
    }

    @Test
    public void test_10() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "select * from llm "
                + "where factId = 'Flight:F100' "
                + "and definition = 'Flight Airport Validation'";

        try {
            compiler.compile(sql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_10_exception", e.getMessage());
            Assertions.assertEquals("projection not supported", e.getMessage());
        }
    }

    @Test
    public void test_11() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String unknownSql = ""
                + "select from llm "
                + "where factId = 'Payment:P200' "
                + "and definition = 'Pay Validation' "
                + "and unknown_flag = 'true'";

        try {
            compiler.compile(unknownSql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_11_unknown_exception", e.getMessage());
            Assertions.assertEquals("unsupported query field: unknown_flag", e.getMessage());
        }

        String controlSql = ""
                + "select from llm "
                + "where factId = 'Payment:P200' "
                + "and definition = 'Pay Validation' "
                + "control "
                + "parameters = 'amount, currency, merchant'";

        try {
            compiler.compile(controlSql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_11_control_exception", e.getMessage());
            Assertions.assertEquals("unsupported query field: control", e.getMessage());
        }

        String specSql = ""
                + "select from llm "
                + "where factId = 'Payment:P200' "
                + "and definition = 'Pay Validation' "
                + "and spec = 'if expression is true then allow, else deny'";

        try {
            compiler.compile(specSql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_11_spec_exception", e.getMessage());
            Assertions.assertEquals("unsupported query field: spec", e.getMessage());
        }

        String parametersSql = ""
                + "select from llm "
                + "where factId = 'Payment:P200' "
                + "and definition = 'Pay Validation' "
                + "and parameters = 'amount, currency, merchant'";

        try {
            compiler.compile(parametersSql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_11_parameters_exception", e.getMessage());
            Assertions.assertEquals("unsupported query field: parameters", e.getMessage());
        }

        String resultSql = ""
                + "select from llm "
                + "where factId = 'Payment:P200' "
                + "and definition = 'Pay Validation' "
                + "and result = 'decision'";

        try {
            compiler.compile(resultSql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_11_result_exception", e.getMessage());
            Assertions.assertEquals("unsupported query field: result", e.getMessage());
        }

        String ruleSql = ""
                + "select from llm "
                + "where factId = 'Payment:P200' "
                + "and definition = 'Pay Validation' "
                + "and rule = 'amount must be present'";

        try {
            compiler.compile(ruleSql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_11_rule_exception", e.getMessage());
            Assertions.assertEquals("unsupported query field: rule", e.getMessage());
        }

        String decisionSql = ""
                + "select from llm "
                + "where factId = 'Payment:P200' "
                + "and definition = 'Pay Validation' "
                + "and decision = 'allow or deny'";

        try {
            compiler.compile(decisionSql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_11_decision_exception", e.getMessage());
            Assertions.assertEquals("unsupported query field: decision", e.getMessage());
        }

        String fifthConstructSql = ""
                + "select from llm "
                + "where factId = 'Payment:P200' "
                + "and relatedFactIds = 'Customer:C1' "
                + "and expression = 'amount > 0' "
                + "and definition = 'Pay Validation' "
                + "and title = 'Pay V1'";

        try {
            compiler.compile(fifthConstructSql);
            Assertions.fail("expected exception");
        } catch (IllegalArgumentException e) {
            Console.log("test_11_fifth_construct_exception", e.getMessage());
            Assertions.assertEquals("unsupported query field: title", e.getMessage());
        }
    }

    @Test
    public void test_12() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String multilineSql = ""
                + "select from llm\n"
                + "where factId = 'Inventory:INV-001'\n"
                + "and relatedFactIds = 'Demand:DEM-001,Supplier:SUP-001'\n"
                + "and expression = 'Inventory.stockStatus is equal to LOW and Demand.level is equal to HIGH and Supplier.availability is equal to AVAILABLE'\n"
                + "and definition = 'Inventory Replenish Decision'";

        String singleLineSql = ""
                + "select from llm "
                + "where factId = 'Inventory:INV-001' "
                + "and relatedFactIds = 'Demand:DEM-001,Supplier:SUP-001' "
                + "and expression = 'Inventory.stockStatus is equal to LOW and Demand.level is equal to HIGH and Supplier.availability is equal to AVAILABLE' "
                + "and definition = 'Inventory Replenish Decision'";

        JsonObject multilineJson = compiler.compile(multilineSql);
        JsonObject singleLineJson = compiler.compile(singleLineSql);

        Console.log("test_12_multiline_json", multilineJson.toString());
        Console.log("test_12_single_line_json", singleLineJson.toString());

        Assertions.assertEquals(multilineJson, singleLineJson);

        JsonObject task = multilineJson.getAsJsonObject("task");
        JsonObject intent = task.getAsJsonObject("intent");
        JsonArray select = task.getAsJsonArray("select");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");

        Assertions.assertEquals("Inventory:INV-001", task.get("factId").getAsString());
        Assertions.assertEquals(2, relatedFactIds.size());
        Assertions.assertEquals("Demand:DEM-001", relatedFactIds.get(0).getAsString());
        Assertions.assertEquals("Supplier:SUP-001", relatedFactIds.get(1).getAsString());

        Assertions.assertNotNull(intent);
        Assertions.assertEquals(0, intent.size());
        Assertions.assertNotNull(select);
        Assertions.assertEquals(0, select.size());

        Assertions.assertEquals("Inventory.stockStatus is equal to LOW and Demand.level is equal to HIGH and Supplier.availability is equal to AVAILABLE", task.get("expression").getAsString());
        Assertions.assertEquals("Inventory Replenish Decision", task.get("definition").getAsString());
        Assertions.assertFalse(task.has("constraints"));
        Assertions.assertFalse(task.has("parameters"));
        Assertions.assertFalse(task.has("spec"));
        Assertions.assertFalse(task.has("result"));
    }
}
