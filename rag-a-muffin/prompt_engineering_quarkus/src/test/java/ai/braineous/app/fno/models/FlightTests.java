package ai.braineous.app.fno.models;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class FlightTests {

    @Test
    public void testJsonSerialization() throws Exception {
        //sample_dataset - flights
        String flightsJsonStr = Resources.getResource("models/fno/models/flight.json");
        JsonObject flightJson = JsonParser.parseString(flightsJsonStr).getAsJsonObject();
        Console.log("flight_json", flightJson);

        Flight flight = new Flight(flightsJsonStr);

        Console.log("flight_str", flight);

        List<Fact> flightFacts = flight.extract(null, null);
        Console.log("flight_facts", flightFacts);
    }
}
