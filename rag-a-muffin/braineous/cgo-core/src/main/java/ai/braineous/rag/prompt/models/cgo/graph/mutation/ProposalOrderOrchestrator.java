package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;

import java.util.*;

class ProposalOrderOrchestrator {
    private static ProposalOrderOrchestrator orch = new ProposalOrderOrchestrator();

    private ProposalOrderOrchestrator() {
    }

    static ProposalOrderOrchestrator getInstance(){
        return orch;
    }

    public Map<String, List<Proposal>> prioritize(Set<Proposal> proposals){
        Map<String, List<Proposal>> proposalMap = new HashMap<>();

        proposalMap.put("accepted", new ArrayList<>());
        proposalMap.put("rejected", new ArrayList<>());

        return proposalMap;
    }
}
