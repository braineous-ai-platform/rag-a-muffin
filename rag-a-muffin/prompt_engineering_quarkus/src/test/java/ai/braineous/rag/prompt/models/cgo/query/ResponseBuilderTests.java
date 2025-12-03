package ai.braineous.rag.prompt.models.cgo.query;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseBuilderTests {
    @Test
    public void testBuildValidationResult() {
        JsonObject result = ResponseBuilder.buildValidResultSimple(
                "Flight:F100",
                "AUS",
                "DFW",
                "Airport:AUS",
                "Airport:DFW"
        );

        // Assertions
        assertEquals("Flight:F100", result.get("flightId").getAsString());
        assertEquals("VALID", result.get("status").getAsString());
        assertTrue(result.getAsJsonObject("checks").get("fromAirportExists").getAsBoolean());
    }
}
