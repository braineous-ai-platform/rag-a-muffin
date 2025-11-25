package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.*;

import java.util.HashSet;
import java.util.Set;

public class ProposalValidator {


    public boolean validate(ProposalContext ctx){
        boolean result;
        Set<Boolean> assertions = new HashSet<>();
        Set<Proposal> proposals = ctx.getProposals();
        Set<FactValidatorRule> factValidatorRules = ctx.getFactValidatorRules();
        Set<RelationshipValidatorRule> relationshipValidatorRules = ctx.getRelationshipValidatorRules();

        for(Proposal proposal: proposals){
            boolean assertion = this.validate(proposal, factValidatorRules, relationshipValidatorRules);
            assertions.add(assertion);
        }

        result = !assertions.contains(false);
        return result;
    }


    private boolean validate(Proposal proposal,
                             Set<FactValidatorRule> factValidatorRules,
                             Set<RelationshipValidatorRule> relationshipValidatorRules){
        boolean result;
        Set<Boolean> assertions = new HashSet<>();
        Set<Fact> inserts = proposal.getInsert();
        Set<Fact> updates = proposal.getUpdate();
        Set<Fact> deletes = proposal.getDelete();
        Set<Relationship> relationships = proposal.getEdges();

        //validate_facts
        for(Fact fact:inserts){
            //TODO: finalize_later
            GraphView view = new GraphView() {
                @Override
                public Fact getFactById(String id) {
                    if ("F1".equals(id)) {
                        return fact;
                    }
                    return null;
                }
            };
            boolean assertion = this.validateFactWithRules(fact, view, factValidatorRules);
            assertions.add(assertion);
        }
        for(Fact fact:updates){
            //TODO: finalize_later
            GraphView view = new GraphView() {
                @Override
                public Fact getFactById(String id) {
                    if ("F1".equals(id)) {
                        return fact;
                    }
                    return null;
                }
            };
            boolean assertion = this.validateFactWithRules(fact, view, factValidatorRules);
            assertions.add(assertion);
        }
        for(Fact fact:deletes){
            //TODO: finalize_later
            GraphView view = new GraphView() {
                @Override
                public Fact getFactById(String id) {
                    if ("F1".equals(id)) {
                        return fact;
                    }
                    return null;
                }
            };
            boolean assertion = this.validateFactWithRules(fact, view, factValidatorRules);
            assertions.add(assertion);
        }

        //validate_relationships
        for(Relationship relationship:relationships){
            //TODO: finalize_later
            /*GraphView view = new GraphView() {
                @Override
                public Fact getFactById(String id) {
                    if ("F1".equals(id)) {
                        return fact;
                    }
                    return null;
                }
            };*/
            boolean assertion = this.validateRelationshipWithRules(relationship, null, relationshipValidatorRules);
            assertions.add(assertion);
        }

        result = !assertions.contains(false);
        return result;
    }

    private boolean validateFactWithRules(Fact fact, GraphView view,
                                          Set<FactValidatorRule> factValidatorRules){
        boolean result;
        Set<Boolean> assertions = new HashSet<>();

        for(FactValidatorRule factValidatorRule: factValidatorRules){
            boolean assertion = this.validateFactWithRule(fact, view, factValidatorRule);
            assertions.add(assertion);
        }

        result = !assertions.contains(false);
        return result;
    }

    private boolean validateFactWithRule(Fact fact, GraphView view,
                                         FactValidatorRule factValidatorRule){
        FactValidatorAdapter adapter = new FactValidatorAdapter();

        // act
        boolean result = adapter.validate(factValidatorRule, fact, view);

        return result;
    }

    private boolean validateRelationshipWithRules(Relationship relationship, GraphView view,
                                                  Set<RelationshipValidatorRule> relationshipValidatorRules){
        boolean result;
        Set<Boolean> assertions = new HashSet<>();

        for(RelationshipValidatorRule relationshipValidatorRule: relationshipValidatorRules){
            boolean assertion = this.validateRelationshipWithRule(relationship, view, relationshipValidatorRule);
            assertions.add(assertion);
        }

        result = !assertions.contains(false);
        return result;
    }

    private boolean validateRelationshipWithRule(Relationship relationship, GraphView view,
                                                  RelationshipValidatorRule relationshipValidatorRule){

        RelationshipValidatorAdapter adapter = new RelationshipValidatorAdapter();

        // act
        boolean result = adapter.validate(relationshipValidatorRule, relationship, view);

        return result;
    }
}
