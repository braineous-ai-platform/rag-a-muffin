package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default JSON-based ValidationResultValidator using Gson.
 *
 * Expected shapes (both supported):
 *
 *  1) Wrapped:
 *     {
 *       "result": {
 *         "ok": true,
 *         "code": "response.contract.ok",
 *         "message": "All good",
 *         "stage": "llm_response_validation",
 *         "anchorId": "Flight:F100",
 *         "metadata": { ... }
 *       }
 *     }
 *
 *  2) Flat:
 *     {
 *       "ok": true,
 *       "code": "response.contract.ok",
 *       "message": "All good",
 *       "stage": "llm_response_validation",
 *       "anchorId": "Flight:F100",
 *       "metadata": { ... }
 *     }
 *
 * On parsing/contract failures:
 *  - returns ValidationResult with ok = false
 *  - code is a generic, domain-agnostic error code
 *  - message is safe for logs / observability
 */
public final class GsonValidationResultValidator implements ValidationResultValidator {

    private static final String DEFAULT_STAGE = "llm_response_validation";

    private final Gson gson;

    public GsonValidationResultValidator() {
        this(new Gson());
    }

    public GsonValidationResultValidator(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    @Override
    public ValidationResult validate(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return ValidationResult.error(
                    "response.contract.empty",
                    "Raw LLM response is empty",
                    DEFAULT_STAGE,
                    null,
                    Collections.singletonMap("rawResponse", rawResponse)
            );
        }

        try {
            JsonElement rootElement = JsonParser.parseString(rawResponse);
            if (!rootElement.isJsonObject()) {
                return ValidationResult.error(
                        "response.contract.root_not_object",
                        "Expected JSON object at root",
                        DEFAULT_STAGE,
                        null,
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            JsonObject root = rootElement.getAsJsonObject();
            JsonObject resultObj = extractResultObject(root);

            if (resultObj == null) {
                return ValidationResult.error(
                        "response.contract.missing_result_object",
                        "Missing 'result' object and root is not a valid result",
                        DEFAULT_STAGE,
                        null,
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            boolean ok = extractBoolean(resultObj, "ok", false);
            String code = extractString(resultObj, "code", ok ? "response.contract.ok" : "response.contract.error");
            String message = extractString(resultObj, "message", "");
            String stage = extractString(resultObj, "stage", DEFAULT_STAGE);
            String anchorId = extractString(resultObj, "anchorId", null);

            Map<String, Object> metadata = extractMetadata(resultObj);

            return ValidationResult.createInternal(ok, code, message, stage, anchorId, metadata);
        } catch (JsonParseException e) {
            return ValidationResult.error(
                    "response.contract.invalid_json",
                    "Failed to parse LLM response as JSON: " + e.getMessage(),
                    DEFAULT_STAGE,
                    null,
                    Collections.singletonMap("rawResponse", rawResponse)
            );
        }
    }

    private static JsonObject extractResultObject(JsonObject root) {
        // Prefer "result" if present and is an object
        if (root.has("result") && root.get("result").isJsonObject()) {
            return root.getAsJsonObject("result");
        }

        // Otherwise treat the root as the result object directly
        return root;
    }

    private static boolean extractBoolean(JsonObject obj, String name, boolean defaultValue) {
        if (!obj.has(name)) {
            return defaultValue;
        }
        JsonElement el = obj.get(name);
        if (el == null || el.isJsonNull()) {
            return defaultValue;
        }
        if (!el.isJsonPrimitive()) {
            return defaultValue;
        }
        JsonPrimitive prim = el.getAsJsonPrimitive();
        if (!prim.isBoolean()) {
            return defaultValue;
        }
        return prim.getAsBoolean();
    }

    private static String extractString(JsonObject obj, String name, String defaultValue) {
        if (!obj.has(name)) {
            return defaultValue;
        }
        JsonElement el = obj.get(name);
        if (el == null || el.isJsonNull()) {
            return defaultValue;
        }
        if (!el.isJsonPrimitive()) {
            return defaultValue;
        }
        JsonPrimitive prim = el.getAsJsonPrimitive();
        if (!prim.isString()) {
            return defaultValue;
        }
        return prim.getAsString();
    }

    private Map<String, Object> extractMetadata(JsonObject resultObj) {
        if (!resultObj.has("metadata") || !resultObj.get("metadata").isJsonObject()) {
            return Collections.emptyMap();
        }

        JsonObject metaObj = resultObj.getAsJsonObject("metadata");
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> meta = gson.fromJson(metaObj, type);

        if (meta == null || meta.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(meta);
    }
}
