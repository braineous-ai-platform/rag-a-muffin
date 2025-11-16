package ai.braineous.rag.prompt.models.cgo.graph;

public class RuleValidator {

    public BindResult bind(Input input){
        boolean ok = input.getFrom().getValidationRule().apply(input.getFrom());

        return new BindResult(ok);
    }
}
