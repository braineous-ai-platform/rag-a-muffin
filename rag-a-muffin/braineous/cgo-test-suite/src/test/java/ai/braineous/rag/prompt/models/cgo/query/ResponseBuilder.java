package ai.braineous.rag.prompt.models.cgo.query;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class ResponseBuilder {

    public enum ValidationStatus {
        VALID,
        INVALID
    }

    /**
     * Build a validation_result JSON object that conforms to the response_contract.
     *
     * This is your canonical builder for tests / dummy responses / non-LLM paths.
     */
    public static JsonObject buildValidationResult(
            String flightId,
            ValidationStatus status,
            String summary,
            String fromAirportCode,
            String toAirportCode,
            String fromAirportNodeId,   // nullable
            String toAirportNodeId,     // nullable
            boolean fromAirportExists,
            boolean toAirportExists,
            boolean fromNotEqualToTo,
            List<String> missingAirports,
            List<String> notes
    ) {
        JsonObject root = new JsonObject();

        // top-level fields
        root.addProperty("flightId", flightId);
        root.addProperty("status", status.name());
        root.addProperty("summary", summary);

        // checks
        JsonObject checks = new JsonObject();
        checks.addProperty("fromAirportCode", fromAirportCode);
        checks.addProperty("toAirportCode", toAirportCode);

        if (fromAirportNodeId != null) {
            checks.addProperty("fromAirportNodeId", fromAirportNodeId);
        } else {
            checks.add("fromAirportNodeId", null);
        }

        if (toAirportNodeId != null) {
            checks.addProperty("toAirportNodeId", toAirportNodeId);
        } else {
            checks.add("toAirportNodeId", null);
        }

        checks.addProperty("fromAirportExists", fromAirportExists);
        checks.addProperty("toAirportExists", toAirportExists);
        checks.addProperty("fromNotEqualToTo", fromNotEqualToTo);

        root.add("checks", checks);

        // diagnostics
        JsonObject diagnostics = new JsonObject();

        JsonArray missingArray = new JsonArray();
        if (missingAirports != null) {
            for (String code : missingAirports) {
                missingArray.add(code);
            }
        }
        diagnostics.add("missingAirports", missingArray);

        JsonArray notesArray = new JsonArray();
        if (notes != null) {
            for (String note : notes) {
                notesArray.add(note);
            }
        }
        diagnostics.add("notes", notesArray);

        root.add("diagnostics", diagnostics);

        return root;
    }

    /**
     * Convenience overload for the common "everything is valid, nothing missing" case.
     */
    public static JsonObject buildValidResultSimple(
            String flightId,
            String fromAirportCode,
            String toAirportCode,
            String fromAirportNodeId,
            String toAirportNodeId
    ) {
        return buildValidationResult(
                flightId,
                ValidationStatus.VALID,
                flightId + " has valid and distinct departure and arrival airport codes present in the graph.",
                fromAirportCode,
                toAirportCode,
                fromAirportNodeId,
                toAirportNodeId,
                true,   // fromAirportExists
                true,   // toAirportExists
                !fromAirportCode.equals(toAirportCode),
                List.of(),      // missingAirports
                List.of(        // notes
                        "Both 'from' and 'to' airport codes map to Airport:* nodes in the context.",
                        "'from' and 'to' are different, so the flight is not a degenerate loop.",
                        "No structural issues detected for this flight with respect to available Airport nodes."
                )
        );
    }
}
