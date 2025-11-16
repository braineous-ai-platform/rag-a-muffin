package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;

public class RelationshipValidatorAdapter {

    public boolean validate(RelationshipValidatorRule rule,
                            Relationship relationship,
                            GraphView view) {

        return rule.validate(relationship, view);
    }
}
