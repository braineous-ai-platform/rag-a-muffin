package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ContextFieldGeneratorTest {

    @Test
    public void supports_shouldReturnTrue_forContextField() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("context");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____context.supported.true____", String.valueOf(supported));

        assertTrue(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldDefinitionIsNull() {
        ContextFieldGenerator generator = new ContextFieldGenerator();

        boolean supported = generator.supports(null);

        Console.log("____context.supported.nullFieldDefinition____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_whenFieldNameIsNull() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition(null);

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____context.supported.nullFieldName____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void supports_shouldReturnFalse_forNonContextField() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("task");

        boolean supported = generator.supports(fieldDefinition);

        Console.log("____context.supported.false____", String.valueOf(supported));

        assertFalse(supported);
    }

    @Test
    public void generate_shouldReturnContextBlock() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("context");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject expected = buildExpectedContextJson();

        Console.log("____context.generate.actual____", result.getFieldValue().toString());
        Console.log("____context.generate.expected____", expected.toString());
        Console.log("____context.generate.validation____", String.valueOf(result.getValidationResult()));

        assertNotNull(result);
        assertSame(fieldDefinition, result.getFieldDefinition());
        assertEquals(expected, result.getFieldValue());

        ValidationResult validationResult = result.getValidationResult();
        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.context.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("context", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    @Test
    public void generate_shouldReturnNodes_withExpectedValues() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("context");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        JsonObject nodes = result.getFieldValue().getAsJsonObject("nodes");
        JsonObject flightNode = nodes.getAsJsonObject("Flight:F100");

        Console.log("____context.nodes.size____", String.valueOf(nodes.entrySet().size()));
        Console.log("____context.node.id____", flightNode.get("id").getAsString());
        Console.log("____context.node.text____", flightNode.get("text").getAsString());
        Console.log("____context.node.attributes____", flightNode.getAsJsonArray("attributes").toString());
        Console.log("____context.node.mode____", flightNode.get("mode").getAsString());

        assertEquals(1, nodes.entrySet().size());
        assertNotNull(flightNode);
        assertEquals("Flight:F100", flightNode.get("id").getAsString());
        assertEquals("{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}", flightNode.get("text").getAsString());
        assertEquals(0, flightNode.getAsJsonArray("attributes").size());
        assertEquals("RELATIONAL", flightNode.get("mode").getAsString());
    }

    @Test
    public void generate_shouldReturnValidationResultAnchoredToContextField() {
        ContextFieldGenerator generator = new ContextFieldGenerator();
        FieldDefinition fieldDefinition = new FieldDefinition("context");
        QueryRequest request = buildRequest();

        FieldGenerationResult result = generator.generate(fieldDefinition, request);

        ValidationResult validationResult = result.getValidationResult();

        Console.log("____context.validationResult____", String.valueOf(validationResult));

        assertNotNull(validationResult);
        assertTrue(validationResult.isOk());
        assertEquals("field.context.ok", validationResult.getCode());
        assertEquals("VALID", validationResult.getMessage());
        assertEquals("field_generation", validationResult.getStage());
        assertEquals("context", validationResult.getAnchorId());
        assertNotNull(validationResult.getMetadata());
        assertTrue(validationResult.getMetadata().isEmpty());
    }

    private QueryRequest buildRequest() {
        Meta meta = new Meta(
                "v1",
                "validate_flight_airports",
                "Validate departure and arrival airport codes"
        );

        Map<String, Node> nodes = new HashMap<String, Node>();
        nodes.put(
                "Flight:F100",
                new Node(
                        "Flight:F100",
                        "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}",
                        Arrays.asList(),
                        Node.Mode.RELATIONAL
                )
        );

        GraphContext context = new GraphContext(nodes);

        ValidateTask task = new ValidateTask(
                "Validate departure and arrival airport codes",
                "Flight:F100",
                Arrays.asList("ok", "code", "message", "anchorId"),
                Arrays.asList("Airport:AUS", "Airport:DFW")
        );

        return new QueryRequest(meta, context, task);
    }

    private JsonObject buildExpectedContextJson() {
        JsonObject expected = new JsonObject();
        JsonObject nodes = new JsonObject();
        JsonObject flightNode = new JsonObject();
        JsonArray attributes = new JsonArray();

        flightNode.addProperty("id", "Flight:F100");
        flightNode.addProperty("text", "{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}");
        flightNode.add("attributes", attributes);
        flightNode.addProperty("mode", "RELATIONAL");

        nodes.add("Flight:F100", flightNode);
        expected.add("nodes", nodes);

        return expected;
    }
}
