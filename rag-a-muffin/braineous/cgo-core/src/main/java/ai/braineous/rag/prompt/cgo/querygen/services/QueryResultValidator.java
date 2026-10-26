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
                        "Missing 'result'",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            if (root.size() != 1) {
                return error(
                        "queryresult.contract.unexpected_fields",
                        "Root must contain exactly one field named 'result'",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            JsonElement resultElement =
                    root.get("result");

            if (resultElement == null
                    || resultElement.isJsonNull()
                    || !resultElement.isJsonPrimitive()
                    || !resultElement.getAsJsonPrimitive().isString()) {
                return error(
                        "queryresult.contract.result_invalid",
                        "'result' must be a JSON string",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            String resultValue =
                    resultElement.getAsString();

            if (resultValue == null || resultValue.isEmpty()) {
                return error(
                        "queryresult.contract.result_empty",
                        "'result' is empty",
                        Collections.singletonMap("rawResponse", rawResponse)
                );
            }

            Map<String, Object> metadata =
                    new HashMap<String, Object>();

            metadata.put("result", resultValue);

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
