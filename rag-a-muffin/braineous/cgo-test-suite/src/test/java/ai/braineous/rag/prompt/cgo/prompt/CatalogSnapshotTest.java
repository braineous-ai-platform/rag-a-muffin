package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogSnapshotTest {

    @Test
    void toJson_omits_blank_fields_and_always_emits_byQueryKind_object() {
        CatalogSnapshot s = new CatalogSnapshot();
        s.setName("   ");
        s.setVersion("\n");
        s.setDescription("  ");
        s.setCreatedAt("\t");
        s.setUpdatedAt("   ");
        s.setByQueryKind(null);

        JsonObject json = s.toJson();

        assertFalse(json.has("name"));
        assertFalse(json.has("version"));
        assertFalse(json.has("description"));
        assertFalse(json.has("createdAt"));
        assertFalse(json.has("updatedAt"));

        assertTrue(json.has("byQueryKind"));
        assertTrue(json.get("byQueryKind").isJsonObject());
        assertEquals(0, json.getAsJsonObject("byQueryKind").size());
    }

    @Test
    void toJson_trims_fields() {
        CatalogSnapshot s = new CatalogSnapshot();
        s.setName("  snap  ");
        s.setVersion("  v1  ");
        s.setDescription("  desc  ");
        s.setCreatedAt("  2026-01-26T10:00:00Z  ");
        s.setUpdatedAt("  2026-01-26T10:01:00Z  ");

        JsonObject json = s.toJson();

        assertEquals("snap", json.get("name").getAsString());
        assertEquals("v1", json.get("version").getAsString());
        assertEquals("desc", json.get("description").getAsString());
        assertEquals("2026-01-26T10:00:00Z", json.get("createdAt").getAsString());
        assertEquals("2026-01-26T10:01:00Z", json.get("updatedAt").getAsString());
    }

    @Test
    void toJson_byQueryKind_uses_entry_safeQueryKind_over_map_key_and_skips_blank_keys_and_null_values() {
        CatalogEntry e1 = new CatalogEntry();
        e1.setQueryKind("  A  "); // normalized -> "A"
        e1.setLlmInstructions(Arrays.asList("x"));

        CatalogEntry e2 = new CatalogEntry();
        e2.setQueryKind("   ");   // safeQueryKind null; fallback to map key
        e2.setLlmInstructions(Arrays.asList("y"));

        Map<String, CatalogEntry> map = new HashMap<String, CatalogEntry>();
        map.put("  ignoredKey  ", e1);  // will serialize under "A"
        map.put("  B  ", e2);           // e2 safeQueryKind null, fallback -> "B"
        map.put("   ", e1);             // blank map key -> skip
        map.put("C", null);             // null value -> skip

        CatalogSnapshot s = new CatalogSnapshot();
        s.setByQueryKind(map);

        JsonObject json = s.toJson();
        JsonObject by = json.getAsJsonObject("byQueryKind");

        assertTrue(by.has("A"));
        assertTrue(by.has("B"));
        assertFalse(by.has("ignoredKey"));
        assertFalse(by.has("C"));

        assertTrue(by.get("A").isJsonObject());
        assertTrue(by.get("B").isJsonObject());

        assertEquals("A", by.getAsJsonObject("A").get("queryKind").getAsString());

        // For B: key is "B" because we fell back to the map key,
        // but the *entry JSON* will not contain queryKind if it was blank.
        assertFalse(by.getAsJsonObject("B").has("queryKind"));
    }

    @Test
    void toJson_deepCopies_embedded_entry_json() {
        CatalogEntry e = new CatalogEntry();
        e.setQueryKind("Q");
        JsonObject rc = new JsonObject();
        rc.addProperty("k", "v1");
        e.setResponseContract(rc);

        Map<String, CatalogEntry> map = new HashMap<String, CatalogEntry>();
        map.put("Q", e);

        CatalogSnapshot s = new CatalogSnapshot();
        s.setByQueryKind(map);

        JsonObject json = s.toJson();
        JsonObject embedded = json.getAsJsonObject("byQueryKind").getAsJsonObject("Q");
        JsonObject embeddedRc = embedded.getAsJsonObject("responseContract");

        assertEquals("v1", embeddedRc.get("k").getAsString());

        // mutate original after serialization
        rc.addProperty("k", "v2");
        assertEquals("v1", embeddedRc.get("k").getAsString());
    }

    @Test
    void fromJson_null_returnsNull() {
        assertNull(CatalogSnapshot.fromJson(null));
    }

    @Test
    void fromJson_happyPath_parses_and_normalizes_map_keys() {
        JsonObject input = new JsonObject();
        input.addProperty("name", "snap");
        input.addProperty("version", "v1");
        input.addProperty("description", "d");
        input.addProperty("createdAt", "c");
        input.addProperty("updatedAt", "u");

        JsonObject by = new JsonObject();

        JsonObject e1 = new JsonObject();
        e1.addProperty("queryKind", "  A  "); // entry says A
        JsonArray instr = new JsonArray();
        instr.add("x");
        e1.add("llmInstructions", instr);
        by.add("ignoredKey", e1); // should land under "A"

        JsonObject e2 = new JsonObject();
        e2.add("llmInstructions", new JsonArray());
        by.add("  B  ", e2); // entry queryKind missing, fallback key "B"

        // junk entries ignored
        by.add("C", new JsonArray());
        by.add("   ", e1);

        input.add("byQueryKind", by);

        CatalogSnapshot s = CatalogSnapshot.fromJson(input);

        assertNotNull(s);
        assertEquals("snap", s.getName());
        assertEquals("v1", s.getVersion());
        assertEquals("d", s.getDescription());
        assertEquals("c", s.getCreatedAt());
        assertEquals("u", s.getUpdatedAt());

        assertNotNull(s.getByQueryKind());
        assertTrue(s.getByQueryKind().containsKey("A"));
        assertTrue(s.getByQueryKind().containsKey("B"));
        assertFalse(s.getByQueryKind().containsKey("ignoredKey"));
        assertFalse(s.getByQueryKind().containsKey("C"));

        assertEquals("  A  ", s.getByQueryKind().get("A").getQueryKind());
        assertNull(s.getByQueryKind().get("B").getQueryKind());
    }

    @Test
    void fromJson_ignores_wrong_types_instead_of_throwing() {
        JsonObject input = new JsonObject();

        input.add("name", new JsonArray());
        input.add("version", new JsonObject());
        input.add("description", new JsonArray());
        input.add("createdAt", new JsonObject());
        input.add("updatedAt", new JsonArray());

        input.add("byQueryKind", new JsonArray()); // wrong type

        CatalogSnapshot s = CatalogSnapshot.fromJson(input);

        assertNotNull(s);
        assertNull(s.getName());
        assertNull(s.getVersion());
        assertNull(s.getDescription());
        assertNull(s.getCreatedAt());
        assertNull(s.getUpdatedAt());

        assertNotNull(s.getByQueryKind());
        assertEquals(0, s.getByQueryKind().size());
    }

    @Test
    void fromJsonString_null_blank_and_nonObject_returnsNull() {
        assertNull(CatalogSnapshot.fromJsonString(null));
        assertNull(CatalogSnapshot.fromJsonString(""));
        assertNull(CatalogSnapshot.fromJsonString("   "));
        assertNull(CatalogSnapshot.fromJsonString("[]"));
        assertNull(CatalogSnapshot.fromJsonString("\"x\""));
        assertNull(CatalogSnapshot.fromJsonString("123"));
        assertNull(CatalogSnapshot.fromJsonString("{"));
    }

    @Test
    void fromJsonString_validObject_parses() {
        String s = "{"
                + "\"name\":\"snap\","
                + "\"version\":\"v1\","
                + "\"byQueryKind\":{"
                + "  \"Q\":{"
                + "    \"queryKind\":\"Q\","
                + "    \"llmInstructions\":[\"a\"]"
                + "  }"
                + "}"
                + "}";

        CatalogSnapshot snap = CatalogSnapshot.fromJsonString(s);

        assertNotNull(snap);
        assertEquals("snap", snap.getName());
        assertEquals("v1", snap.getVersion());

        assertNotNull(snap.getByQueryKind());
        assertTrue(snap.getByQueryKind().containsKey("Q"));
        assertEquals("Q", snap.getByQueryKind().get("Q").getQueryKind());
    }

    @Test
    void roundTrip_toJsonString_then_fromJsonString_preserves_normalized_shape() {
        CatalogEntry e = new CatalogEntry();
        e.setQueryKind("  Q  ");
        e.setLlmInstructions(Arrays.asList(" a ", "", "b"));

        Map<String, CatalogEntry> map = new HashMap<String, CatalogEntry>();
        map.put("ignored", e);

        CatalogSnapshot s = new CatalogSnapshot();
        s.setName("  n  ");
        s.setVersion("  v1  ");
        s.setByQueryKind(map);

        String json = s.toJsonString();
        CatalogSnapshot parsed = CatalogSnapshot.fromJsonString(json);

        assertNotNull(parsed);
        assertEquals("n", parsed.getName());
        assertEquals("v1", parsed.getVersion());

        assertNotNull(parsed.getByQueryKind());
        assertTrue(parsed.getByQueryKind().containsKey("Q"));

        CatalogEntry parsedEntry = parsed.getByQueryKind().get("Q");
        assertNotNull(parsedEntry);
        assertEquals("Q", parsedEntry.getQueryKind());
        assertEquals(2, parsedEntry.getLlmInstructions().size());
        assertEquals("a", parsedEntry.getLlmInstructions().get(0));
        assertEquals("b", parsedEntry.getLlmInstructions().get(1));
    }

    @Test
    void toJsonString_is_valid_json_object_string() {
        CatalogSnapshot s = new CatalogSnapshot();
        s.setName("snap");

        String json = s.toJsonString();
        assertNotNull(json);
        assertTrue(JsonParser.parseString(json).isJsonObject());
    }
}
