package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LLMResponseParserTests {

    @Test
    public void shouldParseValidJsonObject() {

        LLMResponseParser parser = new LLMResponseParser();

        String rawResponse = "{\"answer\":\"ok\",\"success\":true}";

        Console.log("_____llm_response_parser_valid_input_____", rawResponse);

        JsonObject json = parser.parse(rawResponse);

        Console.log("_____llm_response_parser_valid_output_____", json);

        assertNotNull(json);
        assertEquals("ok", json.get("answer").getAsString());
        assertTrue(json.get("success").getAsBoolean());
    }

    @Test
    public void shouldParseNestedJsonObject() {

        LLMResponseParser parser = new LLMResponseParser();

        String rawResponse =
                "{"
                        + "\"result\":{"
                        +     "\"decision\":\"GO\","
                        +     "\"score\":0.98"
                        + "},"
                        + "\"meta\":{"
                        +     "\"model\":\"gpt\""
                        + "}"
                        + "}";

        Console.log("_____llm_response_parser_nested_input_____", rawResponse);

        JsonObject json = parser.parse(rawResponse);

        Console.log("_____llm_response_parser_nested_output_____", json);

        assertNotNull(json);
        assertTrue(json.has("result"));
        assertTrue(json.has("meta"));
        assertEquals("GO", json.getAsJsonObject("result").get("decision").getAsString());
        assertEquals(0.98d, json.getAsJsonObject("result").get("score").getAsDouble());
        assertEquals("gpt", json.getAsJsonObject("meta").get("model").getAsString());
    }

    @Test
    public void shouldParseJsonObjectWithArrayField() {

        LLMResponseParser parser = new LLMResponseParser();

        String rawResponse =
                "{"
                        + "\"choices\":["
                        +     "{\"text\":\"first\"},"
                        +     "{\"text\":\"second\"}"
                        + "]"
                        + "}";

        Console.log("_____llm_response_parser_array_field_input_____", rawResponse);

        JsonObject json = parser.parse(rawResponse);

        Console.log("_____llm_response_parser_array_field_output_____", json);

        assertNotNull(json);
        assertTrue(json.has("choices"));
        assertEquals(2, json.getAsJsonArray("choices").size());
        assertEquals("first", json.getAsJsonArray("choices").get(0).getAsJsonObject().get("text").getAsString());
        assertEquals("second", json.getAsJsonArray("choices").get(1).getAsJsonObject().get("text").getAsString());
    }

    @Test
    public void shouldParseJsonObjectWithWhitespaceAroundIt() {

        LLMResponseParser parser = new LLMResponseParser();

        String rawResponse = "   {\"status\":\"done\"}   ";

        Console.log("_____llm_response_parser_whitespace_input_____", rawResponse);

        JsonObject json = parser.parse(rawResponse);

        Console.log("_____llm_response_parser_whitespace_output_____", json);

        assertNotNull(json);
        assertEquals("done", json.get("status").getAsString());
    }

    @Test
    public void shouldThrowWhenRawResponseIsNull() {

        LLMResponseParser parser = new LLMResponseParser();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        parser.parse(null);
                    }
                }
        );

        Console.log("_____llm_response_parser_null_exception_____", ex.getMessage());

        assertEquals("rawResponse cannot be null", ex.getMessage());
    }

    @Test
    public void shouldThrowWhenRawResponseIsBlank() {

        LLMResponseParser parser = new LLMResponseParser();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        parser.parse("   ");
                    }
                }
        );

        Console.log("_____llm_response_parser_blank_exception_____", ex.getMessage());

        assertEquals("rawResponse cannot be blank", ex.getMessage());
    }

    @Test
    public void shouldThrowWhenRawResponseIsMalformedJson() {

        LLMResponseParser parser = new LLMResponseParser();

        String rawResponse = "{\"answer\":\"ok\"";

        Console.log("_____llm_response_parser_malformed_input_____", rawResponse);

        assertThrows(
                JsonParseException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        parser.parse(rawResponse);
                    }
                }
        );
    }

    @Test
    public void shouldThrowWhenRawResponseIsJsonArrayInsteadOfObject() {

        LLMResponseParser parser = new LLMResponseParser();

        String rawResponse = "[{\"answer\":\"ok\"}]";

        Console.log("_____llm_response_parser_array_root_input_____", rawResponse);

        assertThrows(
                IllegalStateException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        parser.parse(rawResponse);
                    }
                }
        );
    }

    @Test
    public void shouldThrowWhenRawResponseIsJsonPrimitiveInsteadOfObject() {

        LLMResponseParser parser = new LLMResponseParser();

        String rawResponse = "\"hello\"";

        Console.log("_____llm_response_parser_primitive_root_input_____", rawResponse);

        assertThrows(
                IllegalStateException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        parser.parse(rawResponse);
                    }
                }
        );
    }

    @Test
    public void shouldThrowWhenRawResponseIsJsonNullLiteral() {

        LLMResponseParser parser = new LLMResponseParser();

        String rawResponse = "null";

        Console.log("_____llm_response_parser_null_literal_input_____", rawResponse);

        assertThrows(
                IllegalStateException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        parser.parse(rawResponse);
                    }
                }
        );
    }
}
