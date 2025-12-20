package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.*;

import java.util.HashSet;
import java.util.Set;

public class ProposalValidator {
    private static ProposalValidator proposalValidator = new ProposalValidator();

    private ProposalValidator(){

    }

    public static ProposalValidator getInstance(){
        return ProposalValidator.proposalValidator;
    }

    public boolean validate(ProposalContext ctx){
        boolean result;
        Set<Boolean> assertions = new HashSet<>();
        Set<Proposal> proposals = ctx.getProposals();
        GraphSnapshot snapshot = ctx.getSnapshot();

        for(Proposal proposal: proposals){
            boolean assertion = this.validate(snapshot,proposal);
            assertions.add(assertion);
            break;
        }

        result = !assertions.contains(false);
        return result;
    }


    private boolean validate(GraphSnapshot snapshot, Proposal proposal){
        Validator validator = Validator.getInstance();
        boolean result;
        Set<Boolean> assertions = new HashSet<>();

        Set<Fact> inserts = proposal.getInsert();
        Set<Fact> updates = proposal.getUpdate();
        Set<Fact> deletes = proposal.getDelete();
        Set<Relationship> relationships = proposal.getEdges();

        //validate inserts
        for(Fact fact:inserts){
            boolean assertion = validator.validateInsert(fact);
            assertions.add(assertion);
        }

        //validate updates
        for(Fact fact:updates){
            boolean assertion = validator.validateUpdate(snapshot, fact);
            assertions.add(assertion);
        }

        //validate deletes
        for(Fact fact:deletes){
            boolean assertion = validator.validateDelete(fact);
            assertions.add(assertion);
        }

        //validate relationships
        for(Relationship relationship:relationships){
            boolean assertion = validator.validateRelationship(snapshot, relationship);
            assertions.add(assertion);
        }

        result = !assertions.contains(false);
        return result;
    }
}
