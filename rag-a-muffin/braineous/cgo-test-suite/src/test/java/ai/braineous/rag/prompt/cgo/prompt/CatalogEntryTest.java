package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogEntryTest {

    // -------------------------
    // safe* helpers
    // -------------------------

    @Test
    void safeQueryKind_null_and_blank_returnsNull() {
        CatalogEntry e = new CatalogEntry();

        assertNull(e.safeQueryKind());

        e.setQueryKind("   ");
        assertNull(e.safeQueryKind());

        e.setQueryKind("\n\t");
        assertNull(e.safeQueryKind());
    }

    @Test
    void safeQueryKind_trims_nonBlank() {
        CatalogEntry e = new CatalogEntry();
        e.setQueryKind("  user.search  ");
        assertEquals("user.search", e.safeQueryKind());
    }

    @Test
    void safeCatalogVersion_null_and_blank_returnsNull() {
        CatalogEntry e = new CatalogEntry();

        assertNull(e.safeCatalogVersion());

        e.setCatalogVersion("   ");
        assertNull(e.safeCatalogVersion());

        e.setCatalogVersion("\n");
        assertNull(e.safeCatalogVersion());
    }

    @Test
    void safeCatalogVersion_trims_nonBlank() {
        CatalogEntry e = new CatalogEntry();
        e.setCatalogVersion("  v1  ");
        assertEquals("v1", e.safeCatalogVersion());
    }

    @Test
    void safeLlmInstructions_null_returnsEmptyList() {
        CatalogEntry e = new CatalogEntry();
        List<String> list = e.safeLlmInstructions();

        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    void safeLlmInstructions_nonNull_returnsSameListReference() {
        CatalogEntry e = new CatalogEntry();
        List<String> instr = new ArrayList<String>();
        instr.add("a");

        e.setLlmInstructions(instr);

        List<String> safe = e.safeLlmInstructions();
        assertSame(instr, safe);
        assertEquals(1, safe.size());
        assertEquals("a", safe.get(0));
    }

    // -------------------------
    // toJson serialization
    // -------------------------

    @Test
    void toJson_omits_blank_fields_and_always_emits_llmInstructions_array() {
        CatalogEntry e = new CatalogEntry();
        e.setQueryKind("   ");              // omitted
        e.setCatalogVersion("\t");          // omitted
        e.setDescription("  ");             // omitted
        e.setLlmInstructions(null);         // becomes []

        JsonObject json = e.toJson();

        assertFalse(json.has("queryKind"));
        assertFalse(json.has("catalogVersion"));
        assertFalse(json.has("description"));
        assertTrue(json.has("llmInstructions"));
        assertTrue(json.get("llmInstructions").isJsonArray());

        JsonArray arr = json.getAsJsonArray("llmInstructions");
        assertEquals(0, arr.size());
    }

    @Test
    void toJson_trims_queryKind_description_catalogVersion() {
        CatalogEntry e = new CatalogEntry();
        e.setQueryKind("  qk  ");
        e.setDescription("  desc  ");
        e.setCatalogVersion("  1.0.0  ");
        e.setLlmInstructions(Collections.singletonList("  do x  "));

        JsonObject json = e.toJson();

        assertEquals("qk", json.get("queryKind").getAsString());
        assertEquals("desc", json.get("description").getAsString());
        assertEquals("1.0.0", json.get("catalogVersion").getAsString());

        JsonArray arr = json.getAsJsonArray("llmInstructions");
        assertEquals(1, arr.size());
        assertEquals("do x", arr.get(0).getAsString());
    }

    @Test
    void toJson_filters_llmInstructions_null_and_blank_and_trims() {
        CatalogEntry e = new CatalogEntry();
        e.setLlmInstructions(Arrays.asList(
                null,
                "",
                "   ",
                "  a  ",
                "\n",
                "b",
                "  c"
        ));

        JsonObject json = e.toJson();
        JsonArray arr = json.getAsJsonArray("llmInstructions");

        assertEquals(3, arr.size());
        assertEquals("a", arr.get(0).getAsString());
        assertEquals("b", arr.get(1).getAsString());
        assertEquals("c", arr.get(2).getAsString());
    }

    @Test
    void toJson_deepCopies_responseContract_toAvoid_shared_reference() {
        CatalogEntry e = new CatalogEntry();

        JsonObject rc = new JsonObject();
        rc.addProperty("a", "1");
        JsonObject nested = new JsonObject();
        nested.addProperty("n", "x");
        rc.add("nested", nested);

        e.setResponseContract(rc);

        JsonObject json = e.toJson();
        assertTrue(json.has("responseContract"));
        JsonObject copied = json.getAsJsonObject("responseContract");

        // equal content
        assertEquals("1", copied.get("a").getAsString());
        assertEquals("x", copied.getAsJsonObject("nested").get("n").getAsString());

        // not same reference
        assertNotSame(rc, copied);
        assertNotSame(nested, copied.getAsJsonObject("nested"));

        // if original mutates, serialized copy should not change (since it's already built)
        rc.addProperty("a", "2");
        nested.addProperty("n", "y");

        assertEquals("1", copied.get("a").getAsString());
        assertEquals("x", copied.getAsJsonObject("nested").get("n").getAsString());
    }

    // -------------------------
    // fromJson
    // -------------------------

    @Test
    void fromJson_null_returnsNull() {
        assertNull(CatalogEntry.fromJson(null));
    }

    @Test
    void fromJson_happyPath_populates_and_trims_instructions_and_deepCopies_responseContract() {
        JsonObject input = new JsonObject();
        input.addProperty("queryKind", "  qk  ");
        input.addProperty("description", " desc ");
        input.addProperty("catalogVersion", " v1 ");

        JsonObject rc = new JsonObject();
        rc.addProperty("x", "1");
        input.add("responseContract", rc);

        JsonArray arr = new JsonArray();
        arr.add("  a  ");
        arr.add("b");
        arr.add("   ");
        arr.add((String) null);
        input.add("llmInstructions", arr);

        CatalogEntry e = CatalogEntry.fromJson(input);
        assertNotNull(e);

        assertEquals("  qk  ", e.getQueryKind());          // fromJson does not trim field setters
        assertEquals(" desc ", e.getDescription());
        assertEquals(" v1 ", e.getCatalogVersion());

        // but llmInstructions are trimmed and filtered during parsing
        assertNotNull(e.getLlmInstructions());
        assertEquals(2, e.getLlmInstructions().size());
        assertEquals("a", e.getLlmInstructions().get(0));
        assertEquals("b", e.getLlmInstructions().get(1));

        // responseContract deep copy
        assertNotNull(e.getResponseContract());
        assertEquals("1", e.getResponseContract().get("x").getAsString());
        assertNotSame(rc, e.getResponseContract());
    }

    @Test
    void fromJson_ignores_wrong_types_instead_of_throwing() {
        JsonObject input = new JsonObject();

        // queryKind wrong type: object
        JsonObject qkObj = new JsonObject();
        qkObj.addProperty("x", "y");
        input.add("queryKind", qkObj);

        // responseContract wrong type: array
        input.add("responseContract", new JsonArray());

        // llmInstructions wrong type: object
        JsonObject badInstr = new JsonObject();
        badInstr.addProperty("a", "b");
        input.add("llmInstructions", badInstr);

        // description wrong type: array
        input.add("description", new JsonArray());

        // catalogVersion wrong type: object
        input.add("catalogVersion", new JsonObject());

        CatalogEntry e = CatalogEntry.fromJson(input);
        assertNotNull(e);

        assertNull(e.getQueryKind());
        assertNull(e.getResponseContract());
        assertNull(e.getDescription());
        assertNull(e.getCatalogVersion());

        // since llmInstructions was not an array, it never gets set; safe helper returns empty list
        assertNotNull(e.safeLlmInstructions());
        assertEquals(0, e.safeLlmInstructions().size());
    }

    // -------------------------
    // fromJsonString
    // -------------------------

    @Test
    void fromJsonString_null_and_blank_returnsNull() {
        assertNull(CatalogEntry.fromJsonString(null));
        assertNull(CatalogEntry.fromJsonString(""));
        assertNull(CatalogEntry.fromJsonString("   "));
        assertNull(CatalogEntry.fromJsonString("\n\t"));
    }

    @Test
    void fromJsonString_nonJson_returnsNull() {
        assertNull(CatalogEntry.fromJsonString("not json"));
        assertNull(CatalogEntry.fromJsonString("{"));
        assertNull(CatalogEntry.fromJsonString("[]")); // not an object
        assertNull(CatalogEntry.fromJsonString("\"str\""));
        assertNull(CatalogEntry.fromJsonString("123"));
        assertNull(CatalogEntry.fromJsonString("true"));
    }

    @Test
    void fromJsonString_validObject_parses() {
        String s = "{"
                + "\"queryKind\":\"qk\","
                + "\"catalogVersion\":\"v1\","
                + "\"description\":\"d\","
                + "\"llmInstructions\":[\" a \",\"\",\"b\"],"
                + "\"responseContract\":{\"k\":\"v\"}"
                + "}";

        CatalogEntry e = CatalogEntry.fromJsonString(s);

        assertNotNull(e);
        assertEquals("qk", e.getQueryKind());
        assertEquals("v1", e.getCatalogVersion());
        assertEquals("d", e.getDescription());

        assertNotNull(e.getLlmInstructions());
        assertEquals(2, e.getLlmInstructions().size());
        assertEquals("a", e.getLlmInstructions().get(0));
        assertEquals("b", e.getLlmInstructions().get(1));

        assertNotNull(e.getResponseContract());
        assertEquals("v", e.getResponseContract().get("k").getAsString());
    }

    @Test
    void roundTrip_toJsonString_then_fromJsonString_preserves_normalized_shape() {
        CatalogEntry e = new CatalogEntry();
        e.setQueryKind("  qk  ");
        e.setCatalogVersion("  v1  ");
        e.setDescription("  desc  ");
        e.setLlmInstructions(Arrays.asList(" a ", null, " ", "b"));

        JsonObject rc = new JsonObject();
        rc.addProperty("x", "1");
        e.setResponseContract(rc);

        String jsonString = e.toJsonString();
        CatalogEntry parsed = CatalogEntry.fromJsonString(jsonString);

        assertNotNull(parsed);

        // note: fromJsonString stores raw strings as-is from json (already normalized by toJson)
        assertEquals("qk", parsed.getQueryKind());
        assertEquals("v1", parsed.getCatalogVersion());
        assertEquals("desc", parsed.getDescription());

        assertNotNull(parsed.getLlmInstructions());
        assertEquals(2, parsed.getLlmInstructions().size());
        assertEquals("a", parsed.getLlmInstructions().get(0));
        assertEquals("b", parsed.getLlmInstructions().get(1));

        assertNotNull(parsed.getResponseContract());
        assertEquals("1", parsed.getResponseContract().get("x").getAsString());
    }

    @Test
    void toJsonString_is_valid_json_object_string() {
        CatalogEntry e = new CatalogEntry();
        e.setQueryKind("qk");
        e.setLlmInstructions(Collections.singletonList("a"));

        String s = e.toJsonString();
        assertNotNull(s);

        assertTrue(JsonParser.parseString(s).isJsonObject());
    }

    @Test
    void fromJson_llmInstructions_includes_numeric_primitives_as_strings_and_skips_objects_and_blanks() {
        JsonObject input = new JsonObject();
        JsonArray arr = new JsonArray();

        arr.add(" ok ");
        arr.add(123);               // JsonPrimitive number -> getAsString() == "123" (included)
        JsonObject obj = new JsonObject();
        obj.addProperty("x", "y");  // object -> getAsString() throws -> skipped
        arr.add(obj);
        arr.add("   ");             // blank -> skipped
        arr.add("done");

        input.add("llmInstructions", arr);

        CatalogEntry e = CatalogEntry.fromJson(input);

        assertNotNull(e);
        assertNotNull(e.getLlmInstructions());
        assertEquals(3, e.getLlmInstructions().size());
        assertEquals("ok", e.getLlmInstructions().get(0));
        assertEquals("123", e.getLlmInstructions().get(1));
        assertEquals("done", e.getLlmInstructions().get(2));
    }

}

