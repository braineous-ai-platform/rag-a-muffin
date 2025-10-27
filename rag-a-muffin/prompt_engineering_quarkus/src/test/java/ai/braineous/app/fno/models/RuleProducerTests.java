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
        Flight flight = new Flight();

        JsonObject flightJson = new JsonObject();
        flightJson.addProperty("id", "AS5066");
        flightJson.addProperty("number", "AS5066");
        flightJson.addProperty("origin", "MEL");
        flightJson.addProperty("dest", "ADL");

        Console.log("flight", flight);

        List<Fact> facts = flight.extract("", new JsonArray());
        Set<String> rules = this.ruleProducer.produce(facts);
        Console.log("generated_rules", rules);
    }
}
