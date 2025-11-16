package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;

import java.util.Set;

public class EdgeSpec {

    private String fromId;       // Fact.id of source
    private String toId;         // Fact.id of target
    private String kind;         // e.g. "FLIGHT_EDGE", "DEPENDS_ON"
    private Set<String> attributes;  // labels like "valid", "rerouted", etc.
    private EdgeOp op;           // ADD, UPDATE, DELETE

    // getters, setters, constructors...
}
