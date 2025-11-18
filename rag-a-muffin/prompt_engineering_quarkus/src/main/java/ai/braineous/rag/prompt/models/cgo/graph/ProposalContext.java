package ai.braineous.rag.prompt.models.cgo.graph;

import java.util.HashSet;
import java.util.Set;

public class ProposalContext {

    private Set<Proposal> proposals = new HashSet<>();
    private Set<FactValidatorRule> factValidatorRules = new HashSet<>();
    private Set<RelationshipValidatorRule> relationshipValidatorRules = new HashSet<>();

    public Set<Proposal> getProposals() {
        return proposals;
    }

    public void setProposals(Set<Proposal> proposals) {
        this.proposals = proposals;
    }

    public Set<FactValidatorRule> getFactValidatorRules() {
        return factValidatorRules;
    }

    public void setFactValidatorRules(Set<FactValidatorRule> factValidatorRules) {
        this.factValidatorRules = factValidatorRules;
    }

    public Set<RelationshipValidatorRule> getRelationshipValidatorRules() {
        return relationshipValidatorRules;
    }

    public void setRelationshipValidatorRules(Set<RelationshipValidatorRule> relationshipValidatorRules) {
        this.relationshipValidatorRules = relationshipValidatorRules;
    }
}
