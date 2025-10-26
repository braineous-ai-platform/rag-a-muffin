package ai.braineous.app.fno.services;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.utils.Resources;

public class FNOOrchestratorTests {

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private FNOOrchestrator fnoOrchestrator = new FNOOrchestrator();

    @Test
    public void testOrchestrate() throws Exception {
        //flight_events
        String flightEventsStr = Resources.getResource("models/fno/flight_events.json");
        JsonArray flightsArray = JsonParser.parseString(flightEventsStr).getAsJsonArray();

        String eventFactsStr = Resources.getResource("models/fno/facts_fno.json");
        JsonObject eventsJson = JsonParser.parseString(eventFactsStr).getAsJsonObject();
        
        this.fnoOrchestrator.orchestrate(flightsArray);
    }
}
