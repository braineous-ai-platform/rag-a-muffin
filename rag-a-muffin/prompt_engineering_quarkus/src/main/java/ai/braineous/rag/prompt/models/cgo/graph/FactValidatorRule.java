package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;

import java.util.function.Function;

public class FactValidatorRule {

    private String ruleId;

    private Function<Fact, Boolean> rule;
}
