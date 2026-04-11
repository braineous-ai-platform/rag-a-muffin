package ai.braineous.rag.prompt.cgo.querygen.services;

import ai.braineous.rag.prompt.cgo.api.GraphContext;
import ai.braineous.rag.prompt.cgo.api.Meta;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.Node;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.QueryGenOutput;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryGenServiceTest {

    @Test
    public void generateQuery_shouldReturnQueryGenOutput() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);

        Console.log("____queryGenService.output.payload____", output.getPayload().toString());
        Console.log("____queryGenService.output.validation____", String.valueOf(output.getValidationResult()));

        assertNotNull(output);
        assertNotNull(output.getPayload());
        assertNotNull(output.getValidationResult());
    }

    @Test
    public void generateQuery_shouldReturnPayload_withExpectedTopLevelFields() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject payload = output.getPayload();

        Console.log("____queryGenService.payload.meta____", payload.getAsJsonObject("meta").toString());
        Console.log("____queryGenService.payload.context____", payload.getAsJsonObject("context").toString());
        Console.log("____queryGenService.payload.task____", payload.getAsJsonObject("task").toString());
        Console.log("____queryGenService.payload.outputTemplate____", payload.getAsJsonObject("output_template").toString());
        Console.log("____queryGenService.payload.responseContract____", payload.getAsJsonObject("response_contract").toString());
        Console.log("____queryGenService.payload.llmInstructions____", payload.getAsJsonObject("llm_instructions").toString());
        Console.log("____queryGenService.payload.llmTrace____", payload.getAsJsonObject("llm_trace").toString());
        Console.log("____queryGenService.payload.llmTraceInstructions____", payload.getAsJsonObject("llm_trace_instructions").toString());

        assertTrue(payload.has("meta"));
        assertTrue(payload.has("context"));
        assertTrue(payload.has("task"));
        assertTrue(payload.has("output_template"));
        assertTrue(payload.has("response_contract"));
        assertTrue(payload.has("llm_instructions"));
        assertTrue(payload.has("llm_trace"));
        assertTrue(payload.has("llm_trace_instructions"));
    }

    @Test
    public void generateQuery_shouldReturnMeta_withExpectedValues() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject meta = output.getPayload().getAsJsonObject("meta");

        Console.log("____queryGenService.meta.version____", meta.get("version").getAsString());
        Console.log("____queryGenService.meta.queryKind____", meta.get("queryKind").getAsString());
        Console.log("____queryGenService.meta.description____", meta.get("description").getAsString());

        assertEquals("v1", meta.get("version").getAsString());
        assertEquals("validate_flight_airports", meta.get("queryKind").getAsString());
        assertEquals("Validate departure and arrival airport codes", meta.get("description").getAsString());
    }

    @Test
    public void generateQuery_shouldReturnContext_withExpectedNodeValues() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject context = output.getPayload().getAsJsonObject("context");
        JsonObject nodes = context.getAsJsonObject("nodes");
        JsonObject flightNode = nodes.getAsJsonObject("Flight:F100");

        Console.log("____queryGenService.context.nodes.size____", String.valueOf(nodes.entrySet().size()));
        Console.log("____queryGenService.context.node.id____", flightNode.get("id").getAsString());
        Console.log("____queryGenService.context.node.text____", flightNode.get("text").getAsString());
        Console.log("____queryGenService.context.node.attributes____", flightNode.getAsJsonArray("attributes").toString());
        Console.log("____queryGenService.context.node.mode____", flightNode.get("mode").getAsString());

        assertEquals(1, nodes.entrySet().size());
        assertEquals("Flight:F100", flightNode.get("id").getAsString());
        assertEquals("{\"id\":\"F100\",\"kind\":\"Flight\",\"mode\":\"relational\",\"from\":\"AUS\",\"to\":\"DFW\"}", flightNode.get("text").getAsString());
        assertEquals(0, flightNode.getAsJsonArray("attributes").size());
        assertEquals("RELATIONAL", flightNode.get("mode").getAsString());
    }

    @Test
    public void generateQuery_shouldReturnOutputTemplate_withRequestedFieldsInOrder() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject outputTemplate = output.getPayload().getAsJsonObject("output_template");
        JsonObject result = outputTemplate.getAsJsonObject("result");

        Console.log("____queryGenService.outputTemplate____", outputTemplate.toString());
        Console.log("____queryGenService.outputTemplate.result____", result.toString());

        assertNotNull(outputTemplate);
        assertNotNull(result);
        assertEquals(4, result.entrySet().size());

        JsonArray keys = new JsonArray();
        for (Map.Entry<String, com.google.gson.JsonElement> entry : result.entrySet()) {
            keys.add(entry.getKey());
            Console.log("____queryGenService.outputTemplate.key____", entry.getKey());
        }

        assertEquals("ok", keys.get(0).getAsString());
        assertEquals("code", keys.get(1).getAsString());
        assertEquals("message", keys.get(2).getAsString());
        assertEquals("anchorId", keys.get(3).getAsString());

        assertEquals("", result.get("ok").getAsString());
        assertEquals("", result.get("code").getAsString());
        assertEquals("", result.get("message").getAsString());
        assertEquals("", result.get("anchorId").getAsString());
    }

    @Test
    public void generateQuery_shouldReturnResponseContract_withExpectedFields() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject responseContract = output.getPayload().getAsJsonObject("response_contract");
        JsonObject fields = responseContract
                .getAsJsonObject("schema")
                .getAsJsonObject("result")
                .getAsJsonObject("fields");

        Console.log("____queryGenService.responseContract.type____", responseContract.get("type").getAsString());
        Console.log("____queryGenService.responseContract.description____", responseContract.get("description").getAsString());
        Console.log("____queryGenService.responseContract.fields____", fields.toString());

        assertEquals("validation_result", responseContract.get("type").getAsString());
        assertEquals("Deterministic response contract derived from selected fields.", responseContract.get("description").getAsString());

        assertEquals("string", fields.get("ok").getAsString());
        assertEquals("string", fields.get("code").getAsString());
        assertEquals("string", fields.get("message").getAsString());
        assertEquals("string", fields.get("anchorId").getAsString());
    }

    @Test
    public void generateQuery_shouldReturnLlmInstructions_withExpectedStaticAndDynamicOrder() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject llmInstructions = output.getPayload().getAsJsonObject("llm_instructions");
        JsonArray instructions = llmInstructions.getAsJsonArray("instructions");

        Console.log("____queryGenService.llmInstructions.size____", String.valueOf(instructions.size()));
        Console.log("____queryGenService.llmInstructions.0____", instructions.get(0).getAsString());
        Console.log("____queryGenService.llmInstructions.1____", instructions.get(1).getAsString());
        Console.log("____queryGenService.llmInstructions.2____", instructions.get(2).getAsString());
        Console.log("____queryGenService.llmInstructions.3____", instructions.get(3).getAsString());
        Console.log("____queryGenService.llmInstructions.4____", instructions.get(4).getAsString());
        Console.log("____queryGenService.llmInstructions.5____", instructions.get(5).getAsString());
        Console.log("____queryGenService.llmInstructions.6____", instructions.get(6).getAsString());
        Console.log("____queryGenService.llmInstructions.7____", instructions.get(7).getAsString());
        Console.log("____queryGenService.llmInstructions.8____", instructions.get(8).getAsString());
        Console.log("____queryGenService.llmInstructions.9____", instructions.get(9).getAsString());
        Console.log("____queryGenService.llmInstructions.10____", instructions.get(10).getAsString());
        Console.log("____queryGenService.llmInstructions.11____", instructions.get(11).getAsString());
        Console.log("____queryGenService.llmInstructions.12____", instructions.get(12).getAsString());
        Console.log("____queryGenService.llmInstructions.13____", instructions.get(13).getAsString());
        Console.log("____queryGenService.llmInstructions.14____", instructions.get(14).getAsString());
        Console.log("____queryGenService.llmInstructions.15____", instructions.get(15).getAsString());

        assertEquals(16, instructions.size());
        assertEquals("Return ONLY the output_template with values filled.", instructions.get(0).getAsString());
        assertEquals("Use runtime_result as truth.", instructions.get(1).getAsString());
        assertEquals("Do NOT recompute validation from context.", instructions.get(2).getAsString());
        assertEquals("Do not evaluate constraints from scratch.", instructions.get(3).getAsString());
        assertEquals("Return compact JSON on a single line.", instructions.get(4).getAsString());
        assertEquals("Do not include spaces, tabs, or newlines outside JSON syntax.", instructions.get(5).getAsString());
        assertEquals("Set every value as a string.", instructions.get(6).getAsString());
        assertEquals("Return exactly the output_template shape.", instructions.get(7).getAsString());
        assertEquals("Do not add, remove, or rename any fields.", instructions.get(8).getAsString());
        assertEquals("Return exactly one JSON object.", instructions.get(9).getAsString());
        assertEquals("Do not wrap the JSON in markdown fences.", instructions.get(10).getAsString());
        assertEquals("Do not include explanation before or after the JSON.", instructions.get(11).getAsString());
        assertEquals("Set result.ok from runtime_result.ok.", instructions.get(12).getAsString());
        assertEquals("Set result.code from runtime_result.code.", instructions.get(13).getAsString());
        assertEquals("Set result.message from runtime_result.message.", instructions.get(14).getAsString());
        assertEquals("Set result.anchorId from runtime_result.anchorId.", instructions.get(15).getAsString());
    }

    @Test
    public void generateQuery_shouldReturnLlmTrace_withExpectedFields() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject llmTrace = output.getPayload().getAsJsonObject("llm_trace");
        JsonObject fields = llmTrace
                .getAsJsonObject("schema")
                .getAsJsonObject("trace")
                .getAsJsonObject("fields");

        Console.log("____queryGenService.llmTrace.type____", llmTrace.get("type").getAsString());
        Console.log("____queryGenService.llmTrace.description____", llmTrace.get("description").getAsString());
        Console.log("____queryGenService.llmTrace.fields____", fields.toString());

        assertEquals("llm_trace", llmTrace.get("type").getAsString());
        assertEquals("Deterministic internal trace contract for LLM execution profiling.", llmTrace.get("description").getAsString());

        assertEquals("string", fields.get("finish_reason").getAsString());
        assertEquals("string", fields.get("prompt_tokens").getAsString());
        assertEquals("string", fields.get("response_tokens").getAsString());
        assertEquals("string", fields.get("total_tokens").getAsString());
        assertEquals("string", fields.get("latency_ms").getAsString());
        assertEquals("string", fields.get("model_fingerprint").getAsString());
    }

    @Test
    public void generateQuery_shouldReturnLlmTraceInstructions_withExpectedOrder() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject llmTraceInstructions = output.getPayload().getAsJsonObject("llm_trace_instructions");
        JsonArray instructions = llmTraceInstructions.getAsJsonArray("instructions");

        Console.log("____queryGenService.llmTraceInstructions.size____", String.valueOf(instructions.size()));
        Console.log("____queryGenService.llmTraceInstructions.0____", instructions.get(0).getAsString());
        Console.log("____queryGenService.llmTraceInstructions.1____", instructions.get(1).getAsString());
        Console.log("____queryGenService.llmTraceInstructions.2____", instructions.get(2).getAsString());
        Console.log("____queryGenService.llmTraceInstructions.3____", instructions.get(3).getAsString());
        Console.log("____queryGenService.llmTraceInstructions.4____", instructions.get(4).getAsString());
        Console.log("____queryGenService.llmTraceInstructions.5____", instructions.get(5).getAsString());
        Console.log("____queryGenService.llmTraceInstructions.6____", instructions.get(6).getAsString());
        Console.log("____queryGenService.llmTraceInstructions.7____", instructions.get(7).getAsString());

        assertEquals(8, instructions.size());
        assertEquals("Use llm_trace.schema.trace.fields to construct the trace object.", instructions.get(0).getAsString());
        assertEquals("Place all generated trace values under the trace field.", instructions.get(1).getAsString());
        assertEquals("Do not modify the llm_trace section.", instructions.get(2).getAsString());
        assertEquals("Do not add any trace fields not defined in llm_trace.schema.trace.fields.", instructions.get(3).getAsString());
        assertEquals("Do not remove any trace fields defined in llm_trace.schema.trace.fields.", instructions.get(4).getAsString());
        assertEquals("Do not rename any trace fields.", instructions.get(5).getAsString());
        assertEquals("Set every trace field value as a string.", instructions.get(6).getAsString());
        assertEquals("If a trace value cannot be determined, return an empty string for that field.", instructions.get(7).getAsString());
    }

    @Test
    public void generateQuery_shouldReturnTask_withExpectedValues() {
        QueryGenService service = new QueryGenService();
        QueryRequest request = buildRequest();

        QueryGenOutput output = service.generateQuery(request);
        JsonObject task = output.getPayload().getAsJsonObject("task");
        JsonObject intent = task.getAsJsonObject("intent");
        JsonArray selectedFields = task.getAsJsonArray("select");
        JsonArray relatedFactIds = task.getAsJsonArray("relatedFactIds");

        Console.log("____queryGenService.task.intent____", intent.toString());
        Console.log("____queryGenService.task.factId____", task.get("factId").getAsString());
        Console.log("____queryGenService.task.select____", selectedFields.toString());
        Console.log("____queryGenService.task.relatedFactIds____", relatedFactIds.toString());

        assertEquals("Validate departure and arrival airport codes", intent.get("goal").getAsString());
        assertEquals("Flight:F100", task.get("factId").getAsString());

        assertEquals(4, selectedFields.size());
        assertEquals("ok", selectedFields.get(0).getAsString());
        assertEquals("code", selectedFields.get(1).getAsString());
        assertEquals("message", selectedFields.get(2).getAsString());
        assertEquals("anchorId", selectedFields.get(3).getAsString());

        assertEquals(2, relatedFactIds.size());
        assertEquals("Airport:AUS", relatedFactIds.get(0).getAsString());
        assertEquals("Airport:DFW", relatedFactIds.get(1).getAsString());
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
}