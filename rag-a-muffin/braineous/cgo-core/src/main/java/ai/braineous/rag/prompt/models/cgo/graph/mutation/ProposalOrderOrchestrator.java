package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.models.cgo.graph.SnapshotHash;

import java.util.*;

class ProposalOrderOrchestrator {
    private static ProposalOrderOrchestrator orch = new ProposalOrderOrchestrator();

    private ProposalOrderOrchestrator() {
    }

    static ProposalOrderOrchestrator getInstance(){
        return orch;
    }

    public Map<String, List<Proposal>> prioritize(MutationEvent event){
        Map<String, List<Proposal>> proposalMap = new HashMap<>();

        if(event == null) {
            proposalMap.put("accepted", new ArrayList<>());
            proposalMap.put("rejected", new ArrayList<>());
            return proposalMap;
        }

        Set<Proposal> proposals = event.getProposals();
        if(proposals == null || proposals.isEmpty()) {
            proposalMap.put("accepted", new ArrayList<>());
            proposalMap.put("rejected", new ArrayList<>());
            return proposalMap;
        }

        List<Proposal> accepted = new ArrayList<>();
        List<Proposal> rejected = new ArrayList<>();
        proposalMap.put("accepted", accepted);
        proposalMap.put("rejected", rejected);

        //use acceptance policy to validate mutation.
        //for now use one proposal only randomly
        //as there is no criteria that defines
        //a priority
        Proposal proposal = proposals.iterator().next();
        boolean isAccepted = this.isAccepted(event.getSnapshotHash(), proposal);
        if(isAccepted){
            accepted.add(proposal);
        }else{
            rejected.add(proposal);
        }

        return proposalMap;
    }

    private boolean isAccepted(SnapshotHash snapshotHash, Proposal proposal){
        if(snapshotHash == null || proposal == null){
            return false;
        }

        //perform proposal snapshot match with latest
        //graph_store snapshot

        return true;
    }
}
