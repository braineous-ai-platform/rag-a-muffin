package ai.braineous.rag.prompt.models.cgo.graph.data;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FNOFactExtractors {
    //-------generate test data-------------------------
    public static Input okInput() throws Exception{
        //flight_events
        String flightsStr = Resources.getResource("models/fno/flight_validator_ok.json");
        JsonObject flightsJson = JsonParser.parseString(flightsStr).getAsJsonObject();
        JsonArray flightsArray = flightsJson.get("flights").getAsJsonArray();

        Function<String, List<Fact>> okExtractor = new OKExtractor();
        List<Fact> facts = okExtractor.apply(flightsArray.toString());

        assertEquals(facts.size(), 3,
                "make_sure_just_2airports_1_arrival_flight");

        Fact from = facts.get(0);
        Fact to = facts.get(1);
        Fact edge = facts.get(2);

        Input input = new Input(from, to, edge);

        return input;
    }

    //-----fact_extractors-------------------------------
    public static class OKExtractor implements Function<String, List<Fact>> {
        @Override
        public List<Fact> apply(String jsonArrayStr) {
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

                String dstAirportId = "Airport:" + dst;
                String dstAirportText = "Airport(" + dst + ", '" + dst + "')";
                JsonObject dstAirportJson = new JsonObject();
                dstAirportJson.addProperty("id", dstAirportId);
                dstAirportJson.addProperty("kind", "Airport");
                dstAirportJson.addProperty("mode", "atomic");
                // TODO: feats and meta objects

                Fact from = new Fact(
                        srcAirportId,
                        srcAirportJson.toString()
                );
                from.setMode("atomic");
                facts.add(from);

                Fact to = new Fact(
                        dstAirportId,
                        dstAirportJson.toString()
                );
                to.setMode("atomic");
                facts.add(to);

                // Flight fact (canonical)
                String flightText = "Flight(id:'" + id + "', " + src + ", " + dst + ", '" + depZ + "', '" + arrZ + "')";
                JsonObject flightJson = new JsonObject();
                flightJson.addProperty("id", id);
                flightJson.addProperty("kind", "Flight");
                flightJson.addProperty("mode", "relational");
                flightJson.addProperty("from", src);
                flightJson.addProperty("to", dst);
                // TODO: feats and meta objects
                Fact flight = new Fact(
                        "Flight:" + id,
                        flightJson.toString()
                );
                flight.setMode("relational");
                facts.add(flight);
            }

            return facts;
        }
    }

    // ---------- shared helper for "bad" extractors ----------

    public static Input inputFromExtractor(Function<String, List<Fact>> extractor) throws Exception {
        String flightsStr = Resources.getResource("models/fno/flight_validator_ok.json");
        JsonObject flightsJson = JsonParser.parseString(flightsStr).getAsJsonObject();
        JsonArray flightsArray = flightsJson.get("flights").getAsJsonArray();

        List<Fact> facts = extractor.apply(flightsArray.toString());

        assertEquals(3, facts.size(), "expected_3_facts_from_bad_extractor");

        Fact from = facts.get(0);
        Fact to   = facts.get(1);
        Fact edge = facts.get(2);

        return new Input(from, to, edge);
    }

    //------extractors_for_failures------------------------------
    public static class NonAtomicFromExtractor implements Function<String, List<Fact>> {
        @Override
        public List<Fact> apply(String jsonArrayStr) {
            List<Fact> facts = new OKExtractor().apply(jsonArrayStr);
            // flip mode of "from" to non-atomic
            facts.get(0).setMode("relational");
            return facts;
        }
    }

    public static class NonAtomicToExtractor implements Function<String, List<Fact>> {
        @Override
        public List<Fact> apply(String jsonArrayStr) {
            List<Fact> facts = new OKExtractor().apply(jsonArrayStr);
            // flip mode of "to" to non-atomic
            facts.get(1).setMode("relational");
            return facts;
        }
    }

    public static class NonRelationalEdgeExtractor implements Function<String, List<Fact>> {
        @Override
        public List<Fact> apply(String jsonArrayStr) {
            List<Fact> facts = new OKExtractor().apply(jsonArrayStr);
            // flip mode of "edge" to atomic
            facts.get(2).setMode("atomic");
            return facts;
        }
    }

    public static class SelfEdgeExtractor implements Function<String, List<Fact>> {
        @Override
        public List<Fact> apply(String jsonArrayStr) {
            List<Fact> facts = new OKExtractor().apply(jsonArrayStr);

            Fact from = facts.get(0);
            Fact to   = facts.get(1);

            // force same id → self-edge at graph level
            to.setId(from.getId());

            return facts;
        }
    }

    public static class NullModesExtractor implements Function<String, List<Fact>> {
        @Override
        public List<Fact> apply(String jsonArrayStr) {
            List<Fact> facts = new OKExtractor().apply(jsonArrayStr);

            facts.get(0).setMode(null);
            facts.get(1).setMode(null);
            facts.get(2).setMode(null);

            return facts;
        }
    }
    // ---------- tiny Fact helper for null-fact tests ----------

    public static Fact dummyFact(String id, String mode) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        Fact f = new Fact(id, json.toString());
        f.setMode(mode);
        return f;
    }

    //----------ValidationRuleGenerator------------------------
    public static class SimpleValidationRuleGenerator implements Function<Fact, Boolean> {

        /**
         * Applies this function to the given argument.
         *
         * @param fact the function argument
         * @return the function result
         */
        @Override
        public Boolean apply(Fact fact) {
            String id = fact.getId();
            String text = fact.getText();

            Console.log("id", id);
            Console.log("text", text);

            if(id == null || text == null){
                return false;
            }

            return true;
        }
    }
}
