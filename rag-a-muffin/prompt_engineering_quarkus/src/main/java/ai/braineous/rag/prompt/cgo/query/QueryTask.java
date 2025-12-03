package ai.braineous.rag.prompt.cgo.query;

/**
 * Marker/base interface for all query tasks.
 * Each queryKind will have its own concrete implementation.
 */
public interface QueryTask {
    String getDescription();
}

