package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.JsonObject;

/**
 * Generic metadata for a query.
 * queryKind is the semantic operator name.
 */
public final class Meta {

    private final String version;    // e.g. "v1"
    private final String queryKind;  // e.g. "validate_flight_airports"
    private final String description;

    public Meta(String version, String queryKind, String description) {
        this.version = version;
        this.queryKind = queryKind;
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public String getQueryKind() {
        return queryKind;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Meta{" +
                "version='" + version + '\'' +
                ", queryKind='" + queryKind + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    //--------------------------------------------
    public JsonObject toJson() {

        JsonObject out = new JsonObject();

        if (this.version != null) {
            out.addProperty("version", this.version);
        } else {
            out.add("version", null);
        }

        if (this.queryKind != null) {
            out.addProperty("queryKind", this.queryKind);
        } else {
            out.add("queryKind", null);
        }

        if (this.description != null) {
            out.addProperty("description", this.description);
        } else {
            out.add("description", null);
        }

        return out;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    public static Meta fromJson(JsonObject json) {

        if (json == null) {
            throw new IllegalArgumentException("Meta JSON cannot be null");
        }

        String version = null;
        if (json.has("version") && !json.get("version").isJsonNull()) {
            version = json.get("version").getAsString();
        }

        String queryKind = null;
        if (json.has("queryKind") && !json.get("queryKind").isJsonNull()) {
            queryKind = json.get("queryKind").getAsString();
        }

        String description = null;
        if (json.has("description") && !json.get("description").isJsonNull()) {
            description = json.get("description").getAsString();
        }

        return new Meta(version, queryKind, description);
    }


}

