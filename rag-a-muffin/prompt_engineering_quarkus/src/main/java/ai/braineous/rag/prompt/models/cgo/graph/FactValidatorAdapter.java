package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;

public class FactValidatorAdapter {

    public boolean validate(FactValidatorRule rule,
                            Fact fact,
                            GraphView view) {

        return rule.validate(fact, view);
    }
}
