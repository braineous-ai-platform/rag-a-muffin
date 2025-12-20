package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.FactValidatorRule;
import ai.braineous.rag.prompt.cgo.api.RelationshipValidatorRule;

import java.util.HashSet;
import java.util.Set;

public class ProposalContext {

    private Set<Proposal> proposals = new HashSet<>();

    private GraphSnapshot snapshot;

    private Validator validator;



    private boolean validationSuccess;

    public Set<Proposal> getProposals() {
        return proposals;
    }

    public void setProposals(Set<Proposal> proposals) {
        this.proposals = proposals;
    }


    public boolean isValidationSuccess() {
        return validationSuccess;
    }

    public void setValidationSuccess(boolean validationSuccess) {
        this.validationSuccess = validationSuccess;
    }

    public GraphSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(GraphSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}
