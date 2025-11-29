package ai.braineous.rag.prompt.cgo.query;

import ai.braineous.rag.prompt.cgo.api.ValidationResult;

class FakeValidationResultValidator implements ValidationResultValidator {

    private final ValidationResult toReturn;
    private boolean called = false;
    private String lastRawResponse;

    FakeValidationResultValidator(ValidationResult toReturn) {
        this.toReturn = toReturn;
    }

    @Override
    public ValidationResult validate(String rawResponse) {
        this.called = true;
        this.lastRawResponse = rawResponse;
        return toReturn;
    }

    boolean wasCalled() {
        return called;
    }

    String getLastRawResponse() {
        return lastRawResponse;
    }
}


