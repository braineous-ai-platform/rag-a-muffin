package ai.braineous.rag.prompt.cgo.api;

import java.util.List;

public interface RelationshipProvider {

    public List<Relationship> provideRelationships(List<Fact> fact);
}
