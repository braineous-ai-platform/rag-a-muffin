package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTests {

    @Test
    public void testBindOk() throws Exception{
        Validator validator = new Validator();

        Input input = this.okInput();
        BindResult bindResult = validator.bind(input);

        //------assertions--------------------------
        assertTrue(bindResult.isOk(), "binding_is_ok");
    }

    //----Failure cases -----------
    @Test
    public void testBindRejectsNullInput() {
        Validator validator = new Validator();
        BindResult bindResult = validator.bind(null);
        assertFalse(bindResult.isOk(), "null_input_should_fail");
    }

    @Test
    public void testBindRejectsNullFacts() throws Exception {
        Validator validator = new Validator();

        // from = null
        Input input1 = new Input(null, dummyFact("Airport:DFW", "atomic"), dummyFact("Flight:F100", "relational"));
        assertFalse(validator.bind(input1).isOk(), "null_from_should_fail");

        // to = null
        Input input2 = new Input(dummyFact("Airport:AUS", "atomic"), null, dummyFact("Flight:F100", "relational"));
        assertFalse(validator.bind(input2).isOk(), "null_to_should_fail");

        // edge = null
        Input input3 = new Input(dummyFact("Airport:AUS", "atomic"), dummyFact("Airport:DFW", "atomic"), null);
        assertFalse(validator.bind(input3).isOk(), "null_edge_should_fail");
    }

    @Test
    public void testBindRejectsNonAtomicFrom() throws Exception {
        Validator validator = new Validator();
        Input input = this.inputFromExtractor(new NonAtomicFromExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "non_atomic_from_should_fail");
    }

    @Test
    public void testBindRejectsNonAtomicTo() throws Exception {
        Validator validator = new Validator();
        Input input = this.inputFromExtractor(new NonAtomicToExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "non_atomic_to_should_fail");
    }

    @Test
    public void testBindRejectsNonRelationalEdge() throws Exception {
        Validator validator = new Validator();
        Input input = this.inputFromExtractor(new NonRelationalEdgeExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "non_relational_edge_should_fail");
    }

    @Test
    public void testBindRejectsSelfEdge() throws Exception {
        Validator validator = new Validator();
        Input input = this.inputFromExtractor(new SelfEdgeExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "self_edge_should_fail");
    }

    @Test
    public void testBindRejectsNullModes() throws Exception {
        Validator validator = new Validator();
        Input input = this.inputFromExtractor(new NullModesExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "null_modes_should_fail");
    }
    //-------generate test data-------------------------
    private Input okInput() throws Exception{
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
    private static class OKExtractor implements Function<String, List<Fact>> {
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

    private Input inputFromExtractor(Function<String, List<Fact>> extractor) throws Exception {
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
    private static class NonAtomicFromExtractor implements Function<String, List<Fact>> {
        @Override
        public List<Fact> apply(String jsonArrayStr) {
            List<Fact> facts = new OKExtractor().apply(jsonArrayStr);
            // flip mode of "from" to non-atomic
            facts.get(0).setMode("relational");
            return facts;
        }
    }

    private static class NonAtomicToExtractor implements Function<String, List<Fact>> {
        @Override
        public List<Fact> apply(String jsonArrayStr) {
            List<Fact> facts = new OKExtractor().apply(jsonArrayStr);
            // flip mode of "to" to non-atomic
            facts.get(1).setMode("relational");
            return facts;
        }
    }

    private static class NonRelationalEdgeExtractor implements Function<String, List<Fact>> {
        @Override
        public List<Fact> apply(String jsonArrayStr) {
            List<Fact> facts = new OKExtractor().apply(jsonArrayStr);
            // flip mode of "edge" to atomic
            facts.get(2).setMode("atomic");
            return facts;
        }
    }

    private static class SelfEdgeExtractor implements Function<String, List<Fact>> {
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

    private static class NullModesExtractor implements Function<String, List<Fact>> {
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

    private Fact dummyFact(String id, String mode) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        Fact f = new Fact(id, json.toString());
        f.setMode(mode);
        return f;
    }
}
