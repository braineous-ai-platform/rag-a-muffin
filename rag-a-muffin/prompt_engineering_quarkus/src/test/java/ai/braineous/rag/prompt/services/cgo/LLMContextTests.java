package ai.braineous.rag.prompt.services.cgo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class LLMContextTests {

    @Test
    public void testFactGeneration() throws Exception {
        LLMContext context = new LLMContext();

        Console.log("llm_context", context);

        // sample_dataset - flights
        String flightJsonStr = Resources.getResource("models/fno/models/flight.json");
        JsonObject flightJson = JsonParser.parseString(flightJsonStr).getAsJsonObject();
        JsonArray flightsJsonArray = new JsonArray();
        flightsJsonArray.add(flightJson);

        Function<String, List<Fact>> factExtractor = this.getFlightFactExtractor();
        context.build("flights", flightsJsonArray.toString(), factExtractor);

        Console.log("llm_context", context);
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
            Function<String, List<Fact>> factExtractor = this.getFlightFactExtractor();
            context.build("flights", flightJson.toString(), factExtractor);
        } catch (Exception e) {
            Console.log("exception", e.getMessage());
            invalidFormat = true;
        }

        assertTrue(invalidFormat, "invalid_data_format_check_failed");
    }

    private Function<String, List<Fact>> getFlightFactExtractor() {
        Function<String, List<Fact>> flightExtractor = (jsonArrayStr) -> {
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
