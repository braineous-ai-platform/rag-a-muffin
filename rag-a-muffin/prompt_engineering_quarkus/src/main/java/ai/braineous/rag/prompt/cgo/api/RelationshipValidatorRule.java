package ai.braineous.rag.prompt.cgo.api;


@FunctionalInterface
public interface RelationshipValidatorRule {
    boolean validate(Relationship relationship, GraphView view);
}