package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;

import java.util.function.Function;

public class BusinessRule {

    private String ruleId;
    private Function<Fact, Boolean> rule;
}
