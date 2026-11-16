package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapToJsonResponseConverterTest {

    @Test
    public void convert_bareSuccess_returnsScalar() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "success";
        String converted = converter.convert(raw);

        Console.log("map.scalar.bare.success.raw", raw);
        Console.log("map.scalar.bare.success.out", converted);

        Assertions.assertEquals("success", converted);
    }

    @Test
    public void convert_bareFail_returnsScalar() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "fail";
        String converted = converter.convert(raw);

        Console.log("map.scalar.bare.fail.raw", raw);
        Console.log("map.scalar.bare.fail.out", converted);

        Assertions.assertEquals("fail", converted);
    }

    @Test
    public void convert_jsonStringPrimitive_returnsScalar() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "\"success\"";
        String converted = converter.convert(raw);

        Console.log("map.scalar.json.string.raw", raw);
        Console.log("map.scalar.json.string.out", converted);

        Assertions.assertEquals("success", converted);
    }

    @Test
    public void convert_jsonObjectResult_returnsScalar() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "{\"result\":\"success\"}";
        String converted = converter.convert(raw);

        Console.log("map.scalar.object.raw", raw);
        Console.log("map.scalar.object.out", converted);

        Assertions.assertEquals("success", converted);
    }

    @Test
    public void convert_equalsSeparatedSuccess_returnsScalar() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "result=success";
        String converted = converter.convert(raw);

        Console.log("map.scalar.equals.raw", raw);
        Console.log("map.scalar.equals.out", converted);

        Assertions.assertEquals("success", converted);
    }

    @Test
    public void convert_colonSeparatedFail_returnsScalar() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "result: fail";
        String converted = converter.convert(raw);

        Console.log("map.scalar.colon.raw", raw);
        Console.log("map.scalar.colon.out", converted);

        Assertions.assertEquals("fail", converted);
    }

    @Test
    public void convert_markdownFencedSuccess_returnsScalar() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "```\nsuccess\n```";
        String converted = converter.convert(raw);

        Console.log("map.scalar.fence.raw", raw);
        Console.log("map.scalar.fence.out", converted);

        Assertions.assertEquals("success", converted);
    }

    @Test
    public void convert_trimsWhitespace() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "  success  ";
        String converted = converter.convert(raw);

        Console.log("map.scalar.trim.raw", raw);
        Console.log("map.scalar.trim.out", converted);

        Assertions.assertEquals("success", converted);
    }

    @Test
    public void convert_nonContractValue_passthroughUnchanged() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "result=ELIGIBLE";
        String converted = converter.convert(raw);

        Console.log("map.scalar.noise.raw", raw);
        Console.log("map.scalar.noise.out", converted);

        Assertions.assertSame(raw, converted);
        Assertions.assertEquals(raw, converted);
    }

    @Test
    public void convert_missingResult_passthroughUnchanged() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "{\"unused\":true}";
        String converted = converter.convert(raw);

        Console.log("map.scalar.missing.raw", raw);
        Console.log("map.scalar.missing.out", converted);

        Assertions.assertSame(raw, converted);
        Assertions.assertEquals(raw, converted);
    }

    @Test
    public void convert_malformedInput_passthroughUnchanged() {
        MapToJsonResponseConverter converter = new MapToJsonResponseConverter();

        String raw = "{bad-json";
        String converted = converter.convert(raw);

        Console.log("map.scalar.malformed.raw", raw);
        Console.log("map.scalar.malformed.out", converted);

        Assertions.assertSame(raw, converted);
        Assertions.assertEquals(raw, converted);
    }
}
