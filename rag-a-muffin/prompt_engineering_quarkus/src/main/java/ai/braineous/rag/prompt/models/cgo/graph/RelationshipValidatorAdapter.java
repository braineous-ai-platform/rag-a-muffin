package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.cgo.api.RelationshipValidatorRule;

public class RelationshipValidatorAdapter {

    public boolean validate(RelationshipValidatorRule rule,
                            Relationship relationship,
                            GraphView view) {

        return rule.validate(relationship, view);
    }
}
