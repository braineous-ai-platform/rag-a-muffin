package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapToJsonResponseConverterTest {

    @Test
    public void convert_colonSeparatedRecords_buildsExactJsonResult() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "result.decision: allow_capture\n"
                        + "result.reason: primary_payment_request\n"
                        + "result.code: 200";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.colon.raw", raw);
        Console.log("map.json.colon.json", json);
        Console.log("map.json.colon.decision", result.get("decision").getAsString());
        Console.log("map.json.colon.reason", result.get("reason").getAsString());
        Console.log("map.json.colon.code", result.get("code").getAsString());

        this.assertPayResult(result, "allow_capture", "primary_payment_request", "200");
    }

    @Test
    public void convert_equalsSeparatedRecords_buildsExactJsonResult() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "result.decision=allow_capture\n"
                        + "result.reason=primary_payment_request\n"
                        + "result.code=200";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.equals.raw", raw);
        Console.log("map.json.equals.json", json);

        this.assertPayResult(result, "allow_capture", "primary_payment_request", "200");
    }

    @Test
    public void convert_mixedDelimiters_buildsExactJsonResult() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "result.decision: allow_capture\n"
                        + "result.reason=primary_payment_request\n"
                        + "result.code: 200";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.mixed.raw", raw);
        Console.log("map.json.mixed.json", json);

        this.assertPayResult(result, "allow_capture", "primary_payment_request", "200");
    }

    @Test
    public void convert_trimsWhitespaceAroundKeysAndValues() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "  result.decision  :   allow_capture  \n"
                        + "result.reason=   primary_payment_request\n"
                        + "\tresult.code : 200\t";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.trim.raw", raw);
        Console.log("map.json.trim.json", json);

        this.assertPayResult(result, "allow_capture", "primary_payment_request", "200");
    }

    @Test
    public void convert_usesFirstDelimiterWhenBothArePresent() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "result.decision:=allow_capture\n"
                        + "result.reason=:primary_payment_request\n"
                        + "result.code: 200";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.first.delimiter.raw", raw);
        Console.log("map.json.first.delimiter.json", json);
        Console.log("map.json.first.delimiter.decision", result.get("decision").getAsString());
        Console.log("map.json.first.delimiter.reason", result.get("reason").getAsString());

        Assertions.assertEquals("=allow_capture", result.get("decision").getAsString());
        Assertions.assertEquals(":primary_payment_request", result.get("reason").getAsString());
        Assertions.assertEquals("200", result.get("code").getAsString());
        Assertions.assertTrue(result.get("decision").isJsonPrimitive());
        Assertions.assertTrue(result.get("decision").getAsJsonPrimitive().isString());
        Assertions.assertTrue(result.get("reason").getAsJsonPrimitive().isString());
        Assertions.assertTrue(result.get("code").getAsJsonPrimitive().isString());
    }

    @Test
    public void convert_preservesDelimitersInsideValues() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "result.decision: allow_capture\n"
                        + "result.reason: a=b:c\n"
                        + "result.code: 200";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.embedded.raw", raw);
        Console.log("map.json.embedded.json", json);
        Console.log("map.json.embedded.reason", result.get("reason").getAsString());

        Assertions.assertEquals("allow_capture", result.get("decision").getAsString());
        Assertions.assertEquals("a=b:c", result.get("reason").getAsString());
        Assertions.assertEquals("200", result.get("code").getAsString());
        Assertions.assertTrue(result.get("reason").getAsJsonPrimitive().isString());
    }

    @Test
    public void convert_preservesValuesAsJsonStrings() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "result.decision: allow_capture\n"
                        + "result.reason: primary_payment_request\n"
                        + "result.code: 200";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.strings.json", json);
        Console.log("map.json.strings.code.primitive", result.get("code").toString());

        Assertions.assertTrue(result.get("decision").isJsonPrimitive());
        Assertions.assertTrue(result.get("reason").isJsonPrimitive());
        Assertions.assertTrue(result.get("code").isJsonPrimitive());
        Assertions.assertTrue(result.get("decision").getAsJsonPrimitive().isString());
        Assertions.assertTrue(result.get("reason").getAsJsonPrimitive().isString());
        Assertions.assertTrue(result.get("code").getAsJsonPrimitive().isString());
        Assertions.assertEquals("200", result.get("code").getAsString());
        Assertions.assertFalse(result.get("code").getAsJsonPrimitive().isNumber());
    }

    @Test
    public void convert_validJsonObject_passthroughUnchanged() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "{\"result\":{\"decision\":\"CAPTURE\",\"reason\":\"RISK_LEVEL_LOW\",\"code\":\"00\"}}";

        String json = converter.convert(raw);

        Console.log("map.json.passthrough.raw", raw);
        Console.log("map.json.passthrough.json", json);
        Console.log("map.json.passthrough.same", String.valueOf(raw.equals(json)));

        Assertions.assertSame(raw, json);
        Assertions.assertEquals(raw, json);

        JsonObject result = this.resultObject(json);
        this.assertPayResult(result, "CAPTURE", "RISK_LEVEL_LOW", "00");
    }

    @Test
    public void convert_validJsonObjectMissingResult_passthroughUnchanged() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "{\"unused\":true}";

        String json = converter.convert(raw);

        Console.log("map.json.passthrough.unused.raw", raw);
        Console.log("map.json.passthrough.unused.json", json);

        Assertions.assertSame(raw, json);
        Assertions.assertEquals(raw, json);
    }

    @Test
    public void convert_commaSeparatedPayResponse_buildsExactJsonResult() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "result.decision: allow_capture, reason: risk_profile_level_risk, code: 180019";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.comma.pay.raw", raw);
        Console.log("map.json.comma.pay.json", json);

        this.assertPayResult(result, "allow_capture", "risk_profile_level_risk", "180019");
    }

    @Test
    public void convert_commaAndNewlineRecords_buildsExactJsonResult() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "result.decision: allow_capture, reason: primary_payment_request\n"
                        + "result.code: 200";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.comma.lines.raw", raw);
        Console.log("map.json.comma.lines.json", json);

        this.assertPayResult(result, "allow_capture", "primary_payment_request", "200");
    }

    @Test
    public void convert_commaSeparatedMixedDelimiters_buildsExactJsonResult() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw =
                "result.decision: allow_capture, result.reason=primary_payment_request, result.code: 200";

        String json = converter.convert(raw);
        JsonObject result = this.resultObject(json);

        Console.log("map.json.comma.mixed.raw", raw);
        Console.log("map.json.comma.mixed.json", json);

        this.assertPayResult(result, "allow_capture", "primary_payment_request", "200");
    }

    @Test
    public void convert_malformedInput_passthroughUnchanged() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "{bad-json";

        String json = converter.convert(raw);

        Console.log("map.json.malformed.raw", raw);
        Console.log("map.json.malformed.json", json);
        Console.log("map.json.malformed.same", String.valueOf(raw.equals(json)));

        Assertions.assertSame(raw, json);
        Assertions.assertEquals(raw, json);
    }

    private JsonObject resultObject(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        Assertions.assertTrue(root.has("result"));
        Assertions.assertTrue(root.get("result").isJsonObject());
        Assertions.assertEquals(1, root.size());

        JsonObject result = root.getAsJsonObject("result");
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.has("decision"));
        Assertions.assertTrue(result.has("reason"));
        Assertions.assertTrue(result.has("code"));

        return result;
    }

    private void assertPayResult(
            JsonObject result,
            String decision,
            String reason,
            String code
    ) {
        Assertions.assertEquals(decision, result.get("decision").getAsString());
        Assertions.assertEquals(reason, result.get("reason").getAsString());
        Assertions.assertEquals(code, result.get("code").getAsString());
        Assertions.assertTrue(result.get("decision").getAsJsonPrimitive().isString());
        Assertions.assertTrue(result.get("reason").getAsJsonPrimitive().isString());
        Assertions.assertTrue(result.get("code").getAsJsonPrimitive().isString());
    }
}
