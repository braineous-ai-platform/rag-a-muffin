package ai.braineous.rag.prompt.cgo.api;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.observe.Console;
import ai.braineous.rag.prompt.utils.Resources;

import static org.junit.jupiter.api.Assertions.*;

public class LLMContextTests {

    @Test
    public void testFactGeneration() throws Exception {
        Console.log("test.start", "LLMContext.testFactGeneration");

        LLMContext context = new LLMContext();
        Console.log("llm_context.before", context);

        // sample_dataset - flights (single flight object wrapped as array)
        String flightJsonStr =
                Resources.getResource(
                        "models/fno/models/flight.json"
                );

        JsonObject flightJson = JsonParser.parseString(flightJsonStr).getAsJsonObject();
        JsonArray flightsJsonArray = new JsonArray();
        flightsJsonArray.add(flightJson);

        FactExtractor factExtractor = this.getFlightFactExtractor();

        // Minimal RelationshipProvider for this test (no relationships expected)
        RelationshipProvider relationshipProvider = facts -> List.of();

        context.build(
                "flights",
                flightsJsonArray.toString(),
                factExtractor,
                relationshipProvider,
                List.of(),
                List.of(),
                List.of()
        );

        Console.log("llm_context.after", context);
        Console.log("facts.count", context.getAllFacts().size());
        Console.log("relationships.count", context.getAllRelationships().size());

        assertFalse(context.getAllFacts().isEmpty(), "facts should be generated");
        assertEquals(0, context.getAllRelationships().size(), "no relationships expected");
    }


    @Test
    public void testInvalidData() throws Exception {
        LLMContext context = new LLMContext();

        Console.log("llm_context", context);

        // sample_dataset - flights
        boolean invalidFormat = false;
        try {
            String flightJsonStr = Resources.getResource("models/fno/models/flight.json");
            JsonObject flightJson = JsonParser.parseString(flightJsonStr).getAsJsonObject();
            FactExtractor factExtractor = this.getFlightFactExtractor();
            context.build("flights", flightJson.toString(), factExtractor,
                    null, null, null, null);
        } catch (Exception e) {
            Console.log("exception", e.getMessage());
            invalidFormat = true;
        }

        assertTrue(invalidFormat, "invalid_data_format_check_failed");
    }

    @Test
    public void testFactAndRelationshipGeneration() throws Exception {
        Console.log("test.start", "LLMContext.testFactAndRelationshipGeneration");

        LLMContext context = new LLMContext();

        // curated dataset with connections
        String flightsJsonStr =
                ai.braineous.rag.prompt.utils.Resources.getResource(
                        "models/fno/models/flights.json"
                );
        JsonObject root = JsonParser.parseString(flightsJsonStr).getAsJsonObject();
        String arrStr = root.getAsJsonArray("flights").toString();

        FactExtractor factExtractor = new NetworkFactExtractor();
        RelationshipProvider relationshipProvider = new NetworkRelationshipProvider();

        context.build(
                "flights",
                arrStr,
                factExtractor,
                relationshipProvider,
                List.of(),
                List.of(),
                List.of()
        );

        Console.log("facts.count", context.getAllFacts().size());
        Console.log("relationships.count", context.getAllRelationships().size());

        assertEquals(14, context.getAllFacts().size(), "facts count mismatch");
        assertEquals(12, context.getAllRelationships().size(), "relationships count mismatch");
    }
    ///-----------------------------------------------------------------------------

    private FactExtractor getFlightFactExtractor() {
        FactExtractor flightExtractor = (jsonArrayStr) -> {
            List<Fact> facts = new ArrayList<>();

            JsonArray flightsArray = JsonParser.parseString(jsonArrayStr).getAsJsonArray();
            for (int i = 0; i < flightsArray.size(); i++) {
                JsonObject o = flightsArray.get(i).getAsJsonObject();

                String id = o.get("id").getAsString(); // "F102"
                String src = o.get("origin").getAsString(); // "AUS"
                String dst = o.get("dest").getAsString(); // "DFW"
                String depZ = o.get("dep_utc").getAsString(); // "2025-10-22T11:30:00Z"
                String arrZ = o.get("arr_utc").getAsString(); // "2025-10-22T12:40:00Z"

                // Airport facts (one per station)
                String srcAirportId = "Airport:" + src;
                String srcAirportText = "Airport(" + src + ", '" + src + "')";
                String dstAirportId = "Airport:" + dst;
                String dstAirportText = "Airport(" + dst + ", '" + dst + "')";
                facts.add(new Fact(srcAirportId, srcAirportText));
                facts.add(new Fact(dstAirportId, dstAirportText));

                // Flight fact (canonical)
                String flightText = "Flight(id:'" + id + "', " + src + ", " + dst + ", '" + depZ + "', '" + arrZ + "')";
                facts.add(new Fact("Flight:" + id, flightText));
            }

            return facts;
        };
        return flightExtractor;
    }
}
