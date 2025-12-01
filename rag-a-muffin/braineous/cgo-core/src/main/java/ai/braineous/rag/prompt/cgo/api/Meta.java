package ai.braineous.rag.prompt.cgo.api;

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
}

