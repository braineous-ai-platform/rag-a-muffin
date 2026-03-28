package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.*;

import java.util.Collections;
import java.util.Map;

/**
 * Domain-agnostic validation outcome for LLM *response validation*
 * in the CGO query phase.
 *
 * This type must NEVER depend on:
 *  - domain concepts (flight, airport, policy, etc.)
 *  - domain enums
 *  - app-level error codes
 *
 * It is purely structural: "did the LLM response pass validation" + "what, where, when".
 */
public final class ValidationResult {

    /**
     * Whether the validation step succeeded.
     *
     * For LLM responses, this typically means:
     *  - true  -> response conforms to the expected contract/schema
     *  - false -> response is malformed, partial, or contract-violating
     */
    private final boolean ok;

    /**
     * Machine-readable code for the outcome.
     *
     * Examples (all generic, not domain-bound):
     *  - "OK"
     *  - "CONTRACT_VIOLATION"
     *  - "PARSING_FAILED"
     *  - "MISSING_REQUIRED_FIELD"
     *  - "UNEXPECTED_SHAPE"
     */
    private final String code;

    /**
     * Human-readable explanation intended for logs / observability,
     * not necessarily for end-user UX.
     */
    private final String message;

    /**
     * Optional: which phase or component produced this result.
     * e.g. "LLM_RESPONSE_VALIDATION", "QUERY_RESPONSE", "POST_PROCESSING".
     */
    private final String stage;

    /**
     * Optional anchor into the request/response context.
     * Can be a query id, a field path (e.g. "result.items[0].id"), etc.
     * The validator does not interpret it further.
     */
    private final String anchorId;

    /**
     * Optional extra structured data. Completely generic.
     * Callers are free to put whatever they want here (e.g., list of missing fields),
     * but CGO core treats it as opaque.
     */
    private final Map<String, Object> metadata;

    public ValidationResult(
            boolean ok,
            String code,
            String message,
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        this.ok = ok;
        this.code = code;
        this.message = message;
        this.stage = stage;
        this.anchorId = anchorId;
        this.metadata = (metadata == null)
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(metadata);
    }

    // ---------- Static factories (preferred) ----------

    public static ValidationResult ok() {
        return new ValidationResult(true, "OK", "OK", null, null, null);
    }

    public static ValidationResult ok(String stage) {
        return new ValidationResult(true, "OK", "OK", stage, null, null);
    }

    public static ValidationResult ok(
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        return new ValidationResult(true, "OK", "OK", stage, anchorId, metadata);
    }

    public static ValidationResult ok(
            String code,
            String message,
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        return new ValidationResult(true, "OK", "OK", stage, anchorId, metadata);
    }

    public static ValidationResult ok(
            String code,
            String message
    ) {
        // preserve the caller-provided code and message
        return createInternal(
                true,
                code,
                message,
                null,   // no stage in simple factory
                null,   // no anchorId in simple factory
                null    // no metadata in simple factory
        );
    }

    public static ValidationResult error(
            String code,
            String message
    ) {
        return new ValidationResult(false, code, message, null, null, null);
    }

    public static ValidationResult error(
            String code,
            String message,
            String stage,
            String anchorId
    ) {
        return new ValidationResult(false, code, message, stage, anchorId, null);
    }

    public static ValidationResult error(
            String code,
            String message,
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        return new ValidationResult(false, code, message, stage, anchorId, metadata);
    }

    public static ValidationResult createInternal(
            boolean ok,
            String code,
            String message,
            String stage,
            String anchorId,
            Map<String, Object> metadata
    ) {
        return new ValidationResult(
                ok,
                code,
                message,
                stage,
                anchorId,
                metadata
        );
    }


    // ---------- Getters ----------

    public boolean isOk() {
        return ok;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getStage() {
        return stage;
    }

    public String getAnchorId() {
        return anchorId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    // ---------- Convenience ----------

    @Override
    public String toString() {
        return "ValidationResult{" +
                "ok=" + ok +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", stage='" + stage + '\'' +
                ", anchorId='" + anchorId + '\'' +
                ", metadata=" + metadata +
                '}';
    }

    //---------------------------------------
    public JsonObject toJson() {

        JsonObject out = new JsonObject();

        out.addProperty("ok", this.ok);

        if (this.code != null) {
            out.addProperty("code", this.code);
        } else {
            out.add("code", null);
        }

        if (this.message != null) {
            out.addProperty("message", this.message);
        } else {
            out.add("message", null);
        }

        if (this.stage != null) {
            out.addProperty("stage", this.stage);
        } else {
            out.add("stage", null);
        }

        if (this.anchorId != null) {
            out.addProperty("anchorId", this.anchorId);
        } else {
            out.add("anchorId", null);
        }

        // metadata (optional)
        if (this.metadata != null && !this.metadata.isEmpty()) {
            JsonObject meta = new JsonObject();

            for (java.util.Map.Entry<String, Object> e : this.metadata.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();

                if (key == null) {
                    continue;
                }

                meta.add(key, toJsonValue(val));
            }

            out.add("metadata", meta);
        } else {
            out.add("metadata", null);
        }

        return out;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    private static JsonElement toJsonValue(Object val) {

        if (val == null) {
            return JsonNull.INSTANCE;
        }

        if (val instanceof String) {
            return new JsonPrimitive((String) val);
        }

        if (val instanceof Number) {
            return new JsonPrimitive((Number) val);
        }

        if (val instanceof Boolean) {
            return new JsonPrimitive(((Boolean) val).booleanValue());
        }

        if (val instanceof Character) {
            return new JsonPrimitive(String.valueOf(val));
        }

        // arrays / lists of strings (common + safe)
        if (val instanceof java.util.Collection) {
            JsonArray arr = new JsonArray();
            for (Object o : (java.util.Collection) val) {
                if (o == null) {
                    arr.add(JsonNull.INSTANCE);
                } else {
                    arr.add(String.valueOf(o));
                }
            }
            return arr;
        }

        // fallback: opaque string
        return new JsonPrimitive(String.valueOf(val));
    }

    public static ValidationResult fromJson(JsonObject json) {

        if (json == null) {
            throw new IllegalArgumentException("ValidationResult JSON cannot be null");
        }

        boolean ok = false;
        if (json.has("ok") && !json.get("ok").isJsonNull()) {
            ok = json.get("ok").getAsBoolean();
        }

        String code = null;
        if (json.has("code") && !json.get("code").isJsonNull()) {
            code = json.get("code").getAsString();
        }

        String message = null;
        if (json.has("message") && !json.get("message").isJsonNull()) {
            message = json.get("message").getAsString();
        }

        String stage = null;
        if (json.has("stage") && !json.get("stage").isJsonNull()) {
            stage = json.get("stage").getAsString();
        }

        String anchorId = null;
        if (json.has("anchorId") && !json.get("anchorId").isJsonNull()) {
            anchorId = json.get("anchorId").getAsString();
        }

        java.util.Map<String, Object> metadata = null;
        if (json.has("metadata") && !json.get("metadata").isJsonNull() && json.get("metadata").isJsonObject()) {
            JsonObject metaObj = json.getAsJsonObject("metadata");
            metadata = new java.util.HashMap<>();

            for (java.util.Map.Entry<String, JsonElement> e : metaObj.entrySet()) {
                metadata.put(e.getKey(), fromJsonValue(e.getValue()));
            }
        }

        return ValidationResult.createInternal(ok, code, message, stage, anchorId, metadata);
    }

    private static Object fromJsonValue(JsonElement el) {

        if (el == null || el.isJsonNull()) {
            return null;
        }

        if (el.isJsonPrimitive()) {
            com.google.gson.JsonPrimitive p = el.getAsJsonPrimitive();

            if (p.isBoolean()) {
                return Boolean.valueOf(p.getAsBoolean());
            }

            if (p.isNumber()) {
                // keep as String-safe Number? use Double to avoid surprises
                return p.getAsNumber();
            }

            return p.getAsString();
        }

        if (el.isJsonArray()) {
            java.util.List<String> list = new java.util.ArrayList<>();
            for (JsonElement item : el.getAsJsonArray()) {
                if (item == null || item.isJsonNull()) {
                    list.add(null);
                } else if (item.isJsonPrimitive()) {
                    list.add(item.getAsString());
                } else {
                    list.add(item.toString());
                }
            }
            return list;
        }

        // objects/other → opaque string
        return el.toString();
    }


}

