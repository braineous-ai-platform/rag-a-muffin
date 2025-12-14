package ai.braineous.rag.prompt.cgo.api;


import ai.braineous.rag.prompt.cgo.query.QueryTask;
import com.google.gson.JsonObject;

/**
 * Domain-specific request payload for validate_flight_airports.
 * Note the generic "factId" (maps to Graph/Fact id).
 */
public final class ValidateTask implements QueryTask {

    private final String description;
    private final String factId; // e.g. "Flight:F100"

    public ValidateTask(String description, String factId) {
        this.description = description;
        this.factId = factId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getFactId() {
        return factId;
    }

    public static ValidateTask fromJson(JsonObject jsonObject) {

        if (jsonObject == null) {
            return null;
        }

        String description = jsonObject.has("description")
                ? jsonObject.get("description").getAsString()
                : null;

        String factId = jsonObject.has("factId")
                ? jsonObject.get("factId").getAsString()
                : null;

        return new ValidateTask(description, factId);
    }

    @Override
    public String toString() {
        return "ValidateTask{" +
                "description='" + description + '\'' +
                ", factId='" + factId + '\'' +
                '}';
    }
}
