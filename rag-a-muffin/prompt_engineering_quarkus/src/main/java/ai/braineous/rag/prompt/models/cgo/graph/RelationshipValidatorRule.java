package ai.braineous.rag.prompt.models.cgo.graph;


import java.util.function.Function;

public class RelationshipValidatorRule {

    private String ruleId;

    private Function<Relationship, Boolean> rule;
}
