package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapToJsonResponseConverterTest {

    @Test
    public void convert_equalsSeparatedResult_buildsFlatJson() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "result=ELIGIBLE";
        String json = converter.convert(raw);

        Console.log("map.json.equals.raw", raw);
        Console.log("map.json.equals.json", json);

        this.assertFlatResult(json, "ELIGIBLE");
    }

    @Test
    public void convert_colonSeparatedResult_buildsFlatJson() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "result: ELIGIBLE";
        String json = converter.convert(raw);

        Console.log("map.json.colon.raw", raw);
        Console.log("map.json.colon.json", json);

        this.assertFlatResult(json, "ELIGIBLE");
    }

    @Test
    public void convert_jsonStringResult_buildsFlatJson() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "{\"result\":\"ELIGIBLE\"}";
        String json = converter.convert(raw);

        Console.log("map.json.object.raw", raw);
        Console.log("map.json.object.json", json);

        this.assertFlatResult(json, "ELIGIBLE");
        Assertions.assertEquals("{\"result\":\"ELIGIBLE\"}", json);
    }

    @Test
    public void convert_jsonStringResultValueResult_buildsFlatJson() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "{\"result\": \"result\"}";
        String json = converter.convert(raw);

        Console.log("map.json.object.result.value.raw", raw);
        Console.log("map.json.object.result.value.json", json);

        this.assertFlatResult(json, "result");
        Assertions.assertEquals("{\"result\":\"result\"}", json);
    }

    @Test
    public void convert_markdownFencedEqualsResult_buildsFlatJson() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "```\nresult=ELIGIBLE\n```";
        String json = converter.convert(raw);

        Console.log("map.json.fence.equals.raw", raw);
        Console.log("map.json.fence.equals.json", json);

        this.assertFlatResult(json, "ELIGIBLE");
    }

    @Test
    public void convert_markdownFencedJsonResult_buildsFlatJson() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "```json\n{\"result\":\"ELIGIBLE\"}\n```";
        String json = converter.convert(raw);

        Console.log("map.json.fence.object.raw", raw);
        Console.log("map.json.fence.object.json", json);

        this.assertFlatResult(json, "ELIGIBLE");
    }

    @Test
    public void convert_trimsWhitespaceAroundResultKeyAndValue() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "  result  :   ELIGIBLE  ";
        String json = converter.convert(raw);

        Console.log("map.json.trim.raw", raw);
        Console.log("map.json.trim.json", json);

        this.assertFlatResult(json, "ELIGIBLE");
    }

    @Test
    public void convert_usesFirstDelimiterWhenBothArePresent() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "result:=ELIGIBLE";
        String json = converter.convert(raw);

        Console.log("map.json.first.delimiter.raw", raw);
        Console.log("map.json.first.delimiter.json", json);

        this.assertFlatResult(json, "=ELIGIBLE");
    }

    @Test
    public void convert_preservesDelimitersInsideValues() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "result: a=b:c";
        String json = converter.convert(raw);

        Console.log("map.json.embedded.raw", raw);
        Console.log("map.json.embedded.json", json);

        this.assertFlatResult(json, "a=b:c");
    }

    @Test
    public void convert_preservesExtractedValueAsJsonString() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "result=200";
        String json = converter.convert(raw);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        Console.log("map.json.strings.json", json);

        Assertions.assertTrue(root.get("result").isJsonPrimitive());
        Assertions.assertTrue(root.get("result").getAsJsonPrimitive().isString());
        Assertions.assertEquals("200", root.get("result").getAsString());
        Assertions.assertFalse(root.get("result").getAsJsonPrimitive().isNumber());
    }

    @Test
    public void convert_resultEqualsResult_buildsFlatJson() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "result=result";
        String json = converter.convert(raw);

        Console.log("map.json.result.equals.result.raw", raw);
        Console.log("map.json.result.equals.result.json", json);

        this.assertFlatResult(json, "result");
    }

    @Test
    public void convert_missingResult_passthroughUnchanged() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "{\"unused\":true}";
        String json = converter.convert(raw);

        Console.log("map.json.missing.raw", raw);
        Console.log("map.json.missing.json", json);

        Assertions.assertSame(raw, json);
        Assertions.assertEquals(raw, json);
    }

    @Test
    public void convert_malformedInput_passthroughUnchanged() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "{bad-json";
        String json = converter.convert(raw);

        Console.log("map.json.malformed.raw", raw);
        Console.log("map.json.malformed.json", json);

        Assertions.assertSame(raw, json);
        Assertions.assertEquals(raw, json);
    }

    private void assertFlatResult(String json, String expectedValue) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        Assertions.assertEquals(1, root.size());
        Assertions.assertTrue(root.has("result"));
        Assertions.assertTrue(root.get("result").isJsonPrimitive());
        Assertions.assertTrue(root.get("result").getAsJsonPrimitive().isString());
        Assertions.assertEquals(expectedValue, root.get("result").getAsString());
        Assertions.assertEquals("{\"result\":\"" + expectedValue + "\"}", json);
    }
}
