package ai.braineous.rag.prompt.cgo.api;

import ai.braineous.rag.prompt.cgo.query.QueryTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain-specific request payload for validate_flight_airports.
 *
 * factId = primary anchor under evaluation
 * relatedFactIds = supporting graph anchors
 * requestedFields = deterministic response fields requested by the caller
 */
public final class ValidateTask implements QueryTask {

    private String description;
    private String factId; // e.g. "Flight:F100"
    private List<String> requestedFields;
    private List<String> relatedFactIds;

    public ValidateTask(String description, String factId){
        this.description = description;
        this.factId = factId;
    }

    public ValidateTask(String description,
                        String factId,
                        List<String> requestedFields,
                        List<String> relatedFactIds) {
        this.description = description;
        this.factId = factId;
        this.requestedFields = requestedFields;
        this.relatedFactIds = relatedFactIds;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getFactId() {
        return factId;
    }

    public List<String> getRequestedFields() {
        return requestedFields;
    }

    public List<String> getRelatedFactIds() {
        return relatedFactIds;
    }

    @Override
    public String toString() {
        return "ValidateTask{" +
                "description='" + description + '\'' +
                ", factId='" + factId + '\'' +
                ", requestedFields=" + requestedFields +
                ", relatedFactIds=" + relatedFactIds +
                '}';
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = new JsonObject();

        // intent (from description)
        JsonObject intent = new JsonObject();
        if (this.description != null) {
            intent.addProperty("goal", this.description);
        } else {
            intent.add("goal", null);
        }
        out.add("intent", intent);

        // factId
        if (this.factId != null) {
            out.addProperty("factId", this.factId);
        } else {
            out.add("factId", null);
        }

        // relatedFactIds
        out.add("relatedFactIds", toJsonArray(this.relatedFactIds));

        // select (from requestedFields)
        out.add("select", toJsonArray(this.requestedFields));

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

        // intent.goal → description
        if (jsonObject.has("intent") && jsonObject.get("intent").isJsonObject()) {
            JsonObject intent = jsonObject.getAsJsonObject("intent");
            if (intent.has("goal") && !intent.get("goal").isJsonNull()) {
                description = intent.get("goal").getAsString();
            }
        }

        String factId = null;
        if (jsonObject.has("factId") && !jsonObject.get("factId").isJsonNull()) {
            factId = jsonObject.get("factId").getAsString();
        }

        // select → requestedFields
        List<String> requestedFields = readStringArray(jsonObject, "select");

        // relatedFactIds (same)
        List<String> relatedFactIds = readStringArray(jsonObject, "relatedFactIds");

        return new ValidateTask(description, factId, requestedFields, relatedFactIds);
    }

    private static JsonArray toJsonArray(List<String> items) {
        JsonArray array = new JsonArray();

        if (items == null) {
            return array;
        }

        for (String item : items) {
            if (item != null) {
                array.add(item);
            } else {
                array.add((String) null);
            }
        }

        return array;
    }

    private static List<String> readStringArray(JsonObject jsonObject, String fieldName) {
        List<String> values = new ArrayList<String>();

        if (!jsonObject.has(fieldName) || jsonObject.get(fieldName).isJsonNull()) {
            return values;
        }

        if (!jsonObject.get(fieldName).isJsonArray()) {
            return values;
        }

        JsonArray array = jsonObject.getAsJsonArray(fieldName);
        for (int i = 0; i < array.size(); i++) {
            if (!array.get(i).isJsonNull()) {
                values.add(array.get(i).getAsString());
            } else {
                values.add(null);
            }
        }

        return values;
    }
}