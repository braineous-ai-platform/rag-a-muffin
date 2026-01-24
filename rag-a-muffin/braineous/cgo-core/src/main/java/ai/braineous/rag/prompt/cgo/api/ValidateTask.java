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


    @Override
    public String toString() {
        return "ValidateTask{" +
                "description='" + description + '\'' +
                ", factId='" + factId + '\'' +
                '}';
    }

    //----------------------------------------------------------------
    @Override
    public JsonObject toJson() {

        JsonObject out = new JsonObject();

        if (this.description != null) {
            out.addProperty("description", this.description);
        } else {
            out.add("description", null);
        }

        if (this.factId != null) {
            out.addProperty("factId", this.factId);
        } else {
            out.add("factId", null);
        }

        return out;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    public static ValidateTask fromJson(JsonObject jsonObject) {

        if (jsonObject == null) {
            return null;
        }

        String description = null;
        if (jsonObject.has("description") && !jsonObject.get("description").isJsonNull()) {
            description = jsonObject.get("description").getAsString();
        }

        String factId = null;
        if (jsonObject.has("factId") && !jsonObject.get("factId").isJsonNull()) {
            factId = jsonObject.get("factId").getAsString();
        }

        return new ValidateTask(description, factId);
    }
}
