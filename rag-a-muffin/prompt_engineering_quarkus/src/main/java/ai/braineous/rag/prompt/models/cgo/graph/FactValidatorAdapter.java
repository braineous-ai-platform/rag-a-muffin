package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;

public class FactValidatorAdapter {

    public boolean validate(Fact fact, FactValidatorRule rule){
        return true;
    }
}
