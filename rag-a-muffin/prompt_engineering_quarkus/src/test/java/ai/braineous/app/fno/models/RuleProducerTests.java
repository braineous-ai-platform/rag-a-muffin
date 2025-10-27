package ai.braineous.app.fno.models;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ai.braineous.app.fno.services.FNORuleProducer;
import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;

public class RuleProducerTests {

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private FNORuleProducer ruleProducer = new FNORuleProducer();

    @Test
    public void testFactExtraction() throws Exception {
        JsonObject flightJson = new JsonObject();
        flightJson.addProperty("id", "AS5066");
        flightJson.addProperty("number", "AS5066");
        flightJson.addProperty("origin", "AUS");
        flightJson.addProperty("dest", "DFW");
        flightJson.addProperty("dep_utc", "2025-10-22T10:00:00Z");
        flightJson.addProperty("arr_utc", "2025-10-22T11:10:00Z");

        Flight flight = new Flight(flightJson.toString());
        Console.log("flight", flight);

        List<Fact> facts = flight.extract("", new JsonArray());
        Console.log("generated_facts", facts);

        Set<String> rules = this.ruleProducer.produce(facts);
        Console.log("generated_rules", rules);
    }
}
