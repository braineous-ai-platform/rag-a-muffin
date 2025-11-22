package ai.braineous.rag.prompt.models.cgo.graph;


@FunctionalInterface
public interface RelationshipValidatorRule {
    boolean validate(Relationship relationship, GraphView view);
}