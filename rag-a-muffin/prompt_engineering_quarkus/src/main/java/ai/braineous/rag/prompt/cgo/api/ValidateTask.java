package ai.braineous.rag.prompt.cgo.api;


import ai.braineous.rag.prompt.cgo.query.QueryTask;

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
}
