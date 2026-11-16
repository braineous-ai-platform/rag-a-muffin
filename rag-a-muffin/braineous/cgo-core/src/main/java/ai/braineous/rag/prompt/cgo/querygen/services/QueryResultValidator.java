package ai.braineous.rag.prompt.cgo.querygen.services;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.PhaseResultValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class QueryResultValidator implements PhaseResultValidator {

    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    private static final String DEFAULT_STAGE = "llm_response_validation";

    public QueryResultValidator() {
    }

    public static boolean isAcceptedScalar(String value) {
        return SUCCESS.equals(value) || FAIL.equals(value);
    }

    @Override
    public ValidationResult validate(String rawResponse) {

        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return error(
                    "queryresult.contract.empty",
                    "LLM response is empty",
                    Collections.singletonMap("rawResponse", rawResponse)
            );
        }

        String resultValue = rawResponse.trim();

        if (!isAcceptedScalar(resultValue)) {
            Map<String, Object> metadata = new HashMap<String, Object>();
            metadata.put("rawResponse", rawResponse);
            metadata.put("result", resultValue);
            return error(
                    "queryresult.contract.noise",
                    "Result must be exactly success or fail",
                    metadata
            );
        }

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("result", resultValue);

        return ValidationResult.createInternal(
                true,
                "queryresult.contract.ok",
                "Query result contract is valid",
                DEFAULT_STAGE,
                null,
                metadata
        );
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
