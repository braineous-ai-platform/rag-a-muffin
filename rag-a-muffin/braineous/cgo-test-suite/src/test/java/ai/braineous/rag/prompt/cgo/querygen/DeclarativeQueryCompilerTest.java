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
                + "select ok, code "
                + "from llm "
                + "where factId = 'Flight:F100' "
                + "and relatedFactIds = 'Airport:AUS,Airport:DFW' "
                + "control "
                + "decision_mode = 'validate_customer_address', "
                + "validation.departure_code_required = 'true', "
                + "validation.arrival_code_required = 'true'";

        JsonObject json = compiler.compile(sql);

        Console.log("test_1_json", json.toString());

        JsonObject task = json.getAsJsonObject("task");
        JsonObject intent = task.getAsJsonObject("intent");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
        JsonArray select = task.getAsJsonArray("select");
        JsonObject controls = task.getAsJsonObject("constraints").getAsJsonObject("control");

        Assertions.assertEquals("validate_customer_address", intent.get("type").getAsString());
        Assertions.assertEquals("validate_customer_address", intent.get("goal").getAsString());
        Assertions.assertEquals("Flight:F100", task.get("factId").getAsString());

        Assertions.assertEquals(2, relatedFactIds.size());
        Assertions.assertEquals("Airport:AUS", relatedFactIds.get(0).getAsString());
        Assertions.assertEquals("Airport:DFW", relatedFactIds.get(1).getAsString());

        Assertions.assertEquals(2, select.size());
        Assertions.assertEquals("ok", select.get(0).getAsString());
        Assertions.assertEquals("code", select.get(1).getAsString());

        Assertions.assertEquals("validate_customer_address", controls.get("decision_mode").getAsString());
        Assertions.assertEquals("true", controls.get("validation.departure_code_required").getAsString());
        Assertions.assertEquals("true", controls.get("validation.arrival_code_required").getAsString());
    }

    @Test
    public void test_2() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "select ok, code "
                + "from llm "
                + "where factId = 'Flight:F100' "
                + "and relatedFactIds = 'Airport:AUS, Airport:DFW'";

        JsonObject json = compiler.compile(sql);

        Console.log("test_2_json", json.toString());

        JsonObject task = json.getAsJsonObject("task");
        JsonObject intent = task.getAsJsonObject("intent");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");
        JsonArray select = task.getAsJsonArray("select");
        JsonObject controls = task.getAsJsonObject("constraints").getAsJsonObject("control");

        Assertions.assertEquals("ok", intent.get("type").getAsString());
        Assertions.assertEquals("ok", intent.get("goal").getAsString());
        Assertions.assertEquals("Flight:F100", task.get("factId").getAsString());

        Assertions.assertEquals(2, relatedFactIds.size());
        Assertions.assertEquals("Airport:AUS", relatedFactIds.get(0).getAsString());
        Assertions.assertEquals("Airport:DFW", relatedFactIds.get(1).getAsString());

        Assertions.assertEquals(2, select.size());
        Assertions.assertEquals("ok", select.get(0).getAsString());
        Assertions.assertEquals("code", select.get(1).getAsString());

        Assertions.assertEquals(0, controls.size());
    }

    @Test
    public void test_3() {
        DeclarativeQueryCompiler compiler = new DeclarativeQueryCompiler();

        String sql = ""
                + "select ok, code "
                + "from llm "
                + "where relatedFactIds = 'Airport:AUS,Airport:DFW'";

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
                + "and relatedFactIds = 'Airport:AUS,Airport:DFW'";

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
                + "select ok "
                + "from llm "
                + "where factId = 'Flight:F100' "
                + "and relatedFactIds = 'Airport:AUS, Airport:DFW, Airport:JFK'";

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
                + "select ok, code\n"
                + "from llm\n"
                + "where factId = 'Flight:F100'\n"
                + "and relatedFactIds = 'Airport:AUS,Airport:DFW'\n"
                + "control\n"
                + "decision_mode = 'validate_customer_address',\n"
                + "validation.departure_arrival_must_differ = 'true'\n";

        JsonObject json = compiler.compile(sql);

        Console.log("test_6_json", json.toString());

        JsonObject task = json.getAsJsonObject("task");
        JsonObject controls = task.getAsJsonObject("constraints").getAsJsonObject("control");

        Assertions.assertEquals("Flight:F100", task.get("factId").getAsString());
        Assertions.assertEquals("validate_customer_address",
                task.getAsJsonObject("intent").get("type").getAsString());
        Assertions.assertEquals("true",
                controls.get("validation.departure_arrival_must_differ").getAsString());
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
                + "select ok "
                + "from llm "
                + "where factId = 'Flight:F100'";

        JsonObject json = compiler.compile(sql);

        Console.log("test_8_json", json.toString());

        JsonObject task = json.getAsJsonObject("task");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");

        Assertions.assertEquals("Flight:F100", task.get("factId").getAsString());
        Assertions.assertEquals(0, relatedFactIds.size());
    }
}