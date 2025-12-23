package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ProposalOrderOrchestrator {
    private static ProposalOrderOrchestrator orch = new ProposalOrderOrchestrator();

    private ProposalOrderOrchestrator() {
    }

    static ProposalOrderOrchestrator getInstance(){
        return orch;
    }

    public Map<String, List<Proposal>> prioritize(Set<Proposal> proposals){
        //TODO: implement_this
        return new HashMap<>();
    }
}
