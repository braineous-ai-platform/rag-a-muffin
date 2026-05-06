package ai.braineous.rag.prompt.cgo.querygen.services;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class QueryResultValidator implements PhaseResultValidator {

    private static final String DEFAULT_STAGE = "llm_response_validation";

    public QueryResultValidator() {
    }

    @Override
    public ValidationResult validate(String rawResponse) {

        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return error(
                    "queryresult.contract.empty",
                    "LLM response JSON is empty",
                    Collections.singletonMap("rawResponse", rawResponse)
            );
        }

        try {

            JsonElement rootElement =
                    JsonParser.parseString(rawResponse);

            if (!rootElement.isJsonObject()) {
                return error(
                        "queryresult.contract.root_not_object",
                        "Expected JSON object at root",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            JsonObject root =
                    rootElement.getAsJsonObject();

            if (!root.has("result")) {
                return error(
                        "queryresult.contract.result_missing",
                        "Missing 'result' object",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            if (!root.get("result").isJsonObject()) {
                return error(
                        "queryresult.contract.result_invalid",
                        "'result' must be a JSON object",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            JsonObject result =
                    root.getAsJsonObject("result");

            if (result.entrySet().isEmpty()) {
                return error(
                        "queryresult.contract.result_empty",
                        "'result' object is empty",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            for (Map.Entry<String, JsonElement> entry : result.entrySet()) {

                String fieldName =
                        entry.getKey();

                JsonElement fieldValue =
                        entry.getValue();

                if (fieldValue == null
                        || fieldValue.isJsonNull()
                        || !fieldValue.isJsonPrimitive()
                        || !fieldValue.getAsJsonPrimitive().isString()) {

                    return error(
                            "queryresult.contract.result.field_invalid",
                            "Result field must be a string: " + fieldName,
                            Collections.singletonMap("result", result.toString())
                    );
                }
            }

            Map<String, Object> metadata =
                    new HashMap<String, Object>();

            metadata.put("resultFields", result.keySet().toString());

            return ValidationResult.createInternal(
                    true,
                    "queryresult.contract.ok",
                    "Query result contract is valid",
                    DEFAULT_STAGE,
                    null,
                    metadata
            );

        } catch (JsonParseException e) {

            return error(
                    "queryresult.contract.invalid_json",
                    "Failed to parse LLM response as JSON: " + e.getMessage(),
                    Collections.singletonMap("rawResponse", rawResponse)
            );
        }
    }

    private ValidationResult error(String code,
                                   String message,
                                   Map<String, Object> metadata) {

        return ValidationResult.error(
                code,
                message,
                DEFAULT_STAGE,
                null,
                metadata != null
                        ? metadata
                        : Collections.<String, Object>emptyMap()
        );
    }
}