package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.BusinessRule;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.cgo.api.WorldMutation;
import ai.braineous.rag.prompt.observe.Console;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rulepack {
    private List<BusinessRule> rules = new ArrayList<>();

    public List<BusinessRule> getRules() {
        return rules;
    }

    public void setRules(List<BusinessRule> rules) {
        this.rules = rules;
    }

    public Set<Proposal> execute(GraphView view){
        Set<Proposal> proposals = new HashSet<>();

        for(BusinessRule rule: rules){
            try {
                Proposal proposal = new Proposal();

                WorldMutation mutation = rule.execute(view);

                proposal.setInsert(mutation.getInsert());
                proposal.setUpdate(mutation.getUpdate());
                proposal.setDelete(mutation.getDelete());
                proposal.setEdges(mutation.getEdges());

                proposals.add(proposal);
            }catch(Exception e){
                // TODO: route this to ObservabilityEngine (ruleId, exception, context snapshot)
                Console.log("Rulepack.execute: business rule failed", e);
            }
        }

        return proposals;
    }
}
