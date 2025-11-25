package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactValidatorRule;
import ai.braineous.rag.prompt.cgo.api.GraphView;

public class FactValidatorAdapter {

    public boolean validate(FactValidatorRule rule,
                            Fact fact,
                            GraphView view) {

        return rule.validate(fact, view);
    }
}
