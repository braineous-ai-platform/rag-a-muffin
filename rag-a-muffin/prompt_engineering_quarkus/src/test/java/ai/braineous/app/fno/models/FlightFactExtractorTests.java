package ai.braineous.app.fno.models;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;

public class FlightFactExtractorTests {

    @Test
    public void testFactExtraction() throws Exception {
        Flight flight = new Flight();

        flight.setId("AS5066");
        flight.setNumber("AS5066");
        flight.setOrigin(new Airport("MEL", "MEL", ""));
        flight.setDest(new Airport("ADL", "ADL", ""));
        Console.log("flight", flight);

        List<Fact> facts = flight.extract("", new JsonArray());
        Console.log("flight_facts", facts);
    }
}
