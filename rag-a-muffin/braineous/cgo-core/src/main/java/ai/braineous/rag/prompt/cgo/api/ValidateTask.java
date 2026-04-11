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
    private List<Control> controls;

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

    public List<Control> getControls() {
        return controls;
    }

    public void setControls(List<Control> controls) {
        this.controls = controls;
    }

    @Override
    public String toString() {
        return "ValidateTask{" +
                "description='" + description + '\'' +
                ", factId='" + factId + '\'' +
                ", requestedFields=" + requestedFields +
                ", relatedFactIds=" + relatedFactIds +
                ", controls=" + controls +
                '}';
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = new JsonObject();

        JsonObject intent = new JsonObject();
        if (this.description != null) {
            intent.addProperty("goal", this.description);
        } else {
            intent.add("goal", null);
        }
        out.add("intent", intent);

        if (this.factId != null) {
            out.addProperty("factId", this.factId);
        } else {
            out.add("factId", null);
        }

        out.add("relatedFactIds", toJsonArray(this.relatedFactIds));
        out.add("select", toJsonArray(this.requestedFields));
        out.add("controls", toControlsJsonObject(this.controls));

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

        List<String> requestedFields = readStringArray(jsonObject, "select");
        List<String> relatedFactIds = readStringArray(jsonObject, "relatedFactIds");

        ValidateTask task = new ValidateTask(description, factId, requestedFields, relatedFactIds);
        task.setControls(readControls(jsonObject, "controls"));

        return task;
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

    private static JsonObject toControlsJsonObject(List<Control> controls) {
        JsonObject jsonObject = new JsonObject();

        if (controls == null) {
            return jsonObject;
        }

        for (int i = 0; i < controls.size(); i++) {
            Control control = controls.get(i);

            if (control == null) {
                continue;
            }

            String key = control.getKey();
            String value = control.getValue();

            if (key == null) {
                continue;
            }

            if (value != null) {
                jsonObject.addProperty(key, value);
            } else {
                jsonObject.add(key, null);
            }
        }

        return jsonObject;
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

    private static List<Control> readControls(JsonObject jsonObject, String fieldName) {
        List<Control> controls = new ArrayList<Control>();

        if (!jsonObject.has(fieldName) || jsonObject.get(fieldName).isJsonNull()) {
            return controls;
        }

        if (!jsonObject.get(fieldName).isJsonObject()) {
            return controls;
        }

        JsonObject controlsObject = jsonObject.getAsJsonObject(fieldName);
        for (String key : controlsObject.keySet()) {
            if (!controlsObject.get(key).isJsonNull()) {
                controls.add(new Control(key, controlsObject.get(key).getAsString()));
            } else {
                controls.add(new Control(key, null));
            }
        }

        return controls;
    }
}