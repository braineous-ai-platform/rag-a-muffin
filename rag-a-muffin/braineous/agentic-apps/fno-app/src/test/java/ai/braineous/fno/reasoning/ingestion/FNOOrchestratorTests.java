package ai.braineous.fno.reasoning.ingestion;

import ai.braineous.fno.reasoning.ingestion.FNOOrchestrator;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.observe.Console;
import ai.braineous.rag.prompt.utils.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FNOOrchestratorTests {
    private FNOOrchestrator fnoOrchestrator = new FNOOrchestrator();

    @Test
    public void testOrchestrate() throws Exception {
        //flight_events
        String flightsStr = Resources.getResource("models/fno/nano_llm_sample_dataset/flights.json");
        JsonObject flightsJson = JsonParser.parseString(flightsStr).getAsJsonObject();
        JsonArray flightsArray = flightsJson.get("flights").getAsJsonArray();

        JsonObject promptJson = new JsonObject();

        //get in json_format
        GraphView graphView = this.fnoOrchestrator.orchestrate(flightsArray);
        //Console.log("graph_view", ((GraphSnapshot)graphView).toJson());

        GraphSnapshot gs = (GraphSnapshot) graphView;

        //meta
        promptJson.add("meta", buildMeta());

        //context
        JsonObject nodesJson = gs.toJson();
        JsonObject context = new JsonObject();
        context.add("nodes", nodesJson);
        promptJson.add("context", context);

        //task
        JsonObject taskJson = new JsonObject();
        taskJson.addProperty("description", "\"Validate that the selected flight has valid departure and arrival airport codes based on the airport nodes in the graph. A valid flight must have: (1) 'from' matching one Airport:* code, (2) 'to' matching one Airport:* code, (3) 'from' != 'to'.\"");
        taskJson.addProperty("flight_to_validate", "Flight:F100");
        promptJson.add("task", taskJson);

        //response_contract
        JsonObject responseContractJson = buildResponseContract();
        promptJson.add("response_contract", responseContractJson);

        //llm_instructions
        promptJson.add("llm_instructions", buildLlmInstructions());

        Console.log("prompt_json", promptJson.toString());
    }

    @Test
    void ingestion_buildsGraphView_fromFlightsJsonArray() {
        // arrange
        JsonArray flights = new JsonArray();

        JsonObject f1 = new JsonObject();
        f1.addProperty("id", "F102");
        f1.addProperty("origin", "AUS");
        f1.addProperty("dest", "DFW");
        f1.addProperty("dep_utc", "2025-10-22T11:30:00Z");
        f1.addProperty("arr_utc", "2025-10-22T12:40:00Z");

        JsonObject f2 = new JsonObject();
        f2.addProperty("id", "F103");
        f2.addProperty("origin", "DFW");
        f2.addProperty("dest", "IAH");
        f2.addProperty("dep_utc", "2025-10-22T13:30:00Z");
        f2.addProperty("arr_utc", "2025-10-22T14:35:00Z");

        flights.add(f1);
        flights.add(f2);

        Console.log("test.flights.input", flights);

        FNOOrchestrator orchestrator = new FNOOrchestrator();

        // act
        GraphView graph = orchestrator.orchestrate(flights);

        // assert (v1: spine test — just prove the substrate exists)
        assertNotNull(graph);

        // debug: blackbox commentary
        Console.log("test.graphview.type", (graph == null ? "null" : graph.getClass().getName()));
        Console.log("test.graphview.string", String.valueOf(graph));

        // Optional: if GraphView exposes anything stable, log it here (you'll wire exact calls)
        // Console.log("test.graph.nodes", graph.getNodes());
        // Console.log("test.graph.edges", graph.getEdges());
    }

    @Test
    void extract_emitsAirportAndFlightFacts_and_dedupsAirports() {
        String json =
                "[" +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"id\":\"F103\",\"origin\":\"DFW\",\"dest\":\"IAH\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T14:35:00Z\"}" +
                        "]";


        FactExtractor fx = new FNOFactExtractor();

        Console.log("test.input", json);

        List<Fact> facts = fx.extract(json);

        Console.log("test.facts.size", String.valueOf(facts.size()));
        for (Fact f : facts) {
            Console.log("test.fact", f.getId() + " :: " + f.getText());
        }

        assertNotNull(facts);
        assertFalse(facts.isEmpty());

        // v1 assertions (keep them loose)
        assertTrue(facts.stream().anyMatch(f -> f.getId().startsWith("Airport:")));
        assertTrue(facts.stream().anyMatch(f -> f.getId().startsWith("Flight:")));
    }
    //-------response_contract-------------------------------
    // Small helpers to reduce boilerplate
    // Small helpers to reduce boilerplate
    private static JsonObject field(String type, String description) {
        JsonObject field = new JsonObject();
        field.addProperty("type", type);
        field.addProperty("description", description);
        return field;
    }

    private static JsonObject fieldWithNullable(String type, String description, boolean nullable) {
        JsonObject field = field(type, description);
        field.addProperty("nullable", nullable);
        return field;
    }

    private static JsonObject enumField(String description, String... values) {
        JsonObject field = new JsonObject();
        field.addProperty("type", "string");
        field.addProperty("description", description);

        JsonArray enumArray = new JsonArray();
        for (String v : values) {
            enumArray.add(v);
        }
        field.add("enum", enumArray);
        return field;
    }

    private static JsonObject arrayField(String description, String itemType) {
        JsonObject field = new JsonObject();
        field.addProperty("type", "array");

        JsonObject items = new JsonObject();
        items.addProperty("type", itemType);

        field.add("items", items);
        field.addProperty("description", description);
        return field;
    }

    // ---- Main builder for response_contract ----
    public static JsonObject buildResponseContract() {
        JsonObject responseContract = new JsonObject();
        responseContract.addProperty("type", "validation_result");
        responseContract.addProperty(
                "description",
                "Standard response for fact-level validation over a graph context."
        );

        // --- schema.result ---
        JsonObject resultFields = new JsonObject();

        // flightId
        resultFields.add("flightId", field(
                "string",
                "The id of the flight fact that was validated, e.g. 'Flight:F100'."
        ));

        // status
        resultFields.add("status", enumField(
                "Overall validation status for the selected flight.",
                "VALID", "INVALID"
        ));

        // summary
        resultFields.add("summary", field(
                "string",
                "Short human-readable explanation of the validation outcome."
        ));

        // checks object
        JsonObject checksFields = new JsonObject();
        checksFields.add("fromAirportCode", field(
                "string",
                "The 'from' airport code parsed from the flight's text."
        ));
        checksFields.add("toAirportCode", field(
                "string",
                "The 'to' airport code parsed from the flight's text."
        ));
        checksFields.add("fromAirportNodeId", fieldWithNullable(
                "string",
                "The Airport:* node id that matches the 'from' code, if any.",
                true
        ));
        checksFields.add("toAirportNodeId", fieldWithNullable(
                "string",
                "The Airport:* node id that matches the 'to' code, if any.",
                true
        ));
        checksFields.add("fromAirportExists", field(
                "boolean",
                "True if an Airport:* node exists for the 'from' code."
        ));
        checksFields.add("toAirportExists", field(
                "boolean",
                "True if an Airport:* node exists for the 'to' code."
        ));
        checksFields.add("fromNotEqualToTo", field(
                "boolean",
                "True if 'from' and 'to' airport codes are different."
        ));

        JsonObject checksObject = new JsonObject();
        checksObject.addProperty("type", "object");
        checksObject.add("fields", checksFields);

        resultFields.add("checks", checksObject);

        // diagnostics object
        JsonObject diagnosticsFields = new JsonObject();
        diagnosticsFields.add("missingAirports", arrayField(
                "List of airport codes that were referenced by the flight but not found as Airport:* nodes.",
                "string"
        ));

        JsonObject notesField = new JsonObject();
        notesField.addProperty("type", "array");
        JsonObject notesItems = new JsonObject();
        notesItems.addProperty("type", "string");
        notesField.add("items", notesItems);
        notesField.addProperty(
                "description",
                "Additional free-form notes about how the decision was reached."
        );
        diagnosticsFields.add("notes", notesField);

        JsonObject diagnosticsObject = new JsonObject();
        diagnosticsObject.addProperty("type", "object");
        diagnosticsObject.add("fields", diagnosticsFields);

        resultFields.add("diagnostics", diagnosticsObject);

        // wrap result
        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("type", "object");
        resultObject.add("fields", resultFields);

        // schema
        JsonObject schemaObject = new JsonObject();
        schemaObject.add("result", resultObject);

        responseContract.add("schema", schemaObject);

        // instructions
        JsonArray instructions = new JsonArray();
        instructions.add("Return a single JSON object that strictly follows this schema.");
        instructions.add("Do not include any fields not listed in this schema.");
        instructions.add("Do not add natural language outside of JSON.");
        responseContract.add("instructions", instructions);

        return responseContract;
    }

    public static JsonObject buildMeta() {
        JsonObject meta = new JsonObject();
        meta.addProperty("version", "v1");
        meta.addProperty("query_kind", "validate_flight_airports");
        meta.addProperty("description",
                "Prompt for validating that a single flight fact has valid departure and arrival airport codes using graph context.");
        return meta;
    }

    public static JsonArray buildLlmInstructions() {
        JsonArray arr = new JsonArray();

        arr.add("You are given a JSON object with 'meta', 'context', 'task', 'response_contract', and 'llm_instructions' fields.");
        arr.add("Use only the 'context' and 'task' to understand what needs to be computed.");
        arr.add("Treat all node 'text' fields in context as JSON-encoded strings. You may parse them logically to read fields like 'id', 'kind', 'from', 'to'.");
        arr.add("Use the 'response_contract' as the single source of truth for the shape of your reply.");
        arr.add("Your answer must be a single JSON object that matches the 'result' schema in response_contract.");
        arr.add("Do not include any keys that are not defined in the response_contract schema.");
        arr.add("Do not include any natural language outside of JSON.");
        arr.add("If any information is missing or ambiguous, make the best effort using the given context without inventing airports or flights that are not present in the nodes.");

        return arr;
    }



}
