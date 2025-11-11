package ai.braineous.app.fno.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.LLMBridge;
import ai.braineous.rag.prompt.services.cgo.LLMContext;
import ai.braineous.rag.prompt.services.cgo.causal.CausalLLMBridge;
import ai.braineous.rag.prompt.utils.Console;

public class FNOOrchestrator {

    // TODO: [eventually] : make_it_quarkus_containarized.
    // For now no dependency_injection overhead
    // @Inject
    private LLMBridge llmBridge = new CausalLLMBridge();

    public void orchestrate(JsonArray flightsJsonArray) {
        try {
            LLMContext context = new LLMContext();

            Function<String, List<Fact>> factExtractor = this.getFlightFactExtractor();

            context.build("flights",
                    flightsJsonArray.toString(), factExtractor);

            // bridge to CGO
            this.llmBridge.submit(context);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
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
                JsonObject srcAirportJson = new JsonObject();
                srcAirportJson.addProperty("id", srcAirportId);
                srcAirportJson.addProperty("kind", "Airport");
                srcAirportJson.addProperty("mode", "atomic");
                // TODO: feats and meta objects
                Console.log("src_airport", srcAirportJson);

                String dstAirportId = "Airport:" + dst;
                String dstAirportText = "Airport(" + dst + ", '" + dst + "')";
                JsonObject dstAirportJson = new JsonObject();
                dstAirportJson.addProperty("id", dstAirportId);
                dstAirportJson.addProperty("kind", "Airport");
                dstAirportJson.addProperty("mode", "atomic");
                // TODO: feats and meta objects
                Console.log("dst_airport", srcAirportJson);

                facts.add(new Fact(srcAirportId, srcAirportJson.toString()));
                facts.add(new Fact(dstAirportId, dstAirportJson.toString()));

                // Flight fact (canonical)
                String flightText = "Flight(id:'" + id + "', " + src + ", " + dst + ", '" + depZ + "', '" + arrZ + "')";
                JsonObject flightJson = new JsonObject();
                flightJson.addProperty("id", id);
                flightJson.addProperty("kind", "Flight");
                flightJson.addProperty("mode", "relational");
                flightJson.addProperty("from", src);
                flightJson.addProperty("to", dst);
                // TODO: feats and meta objects
                Console.log("flight", flightJson);
                facts.add(new Fact("Flight:" + id, flightJson.toString()));
            }

            return facts;
        };
        return flightExtractor;
    }
}
