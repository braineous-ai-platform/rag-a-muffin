package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.*;

import java.util.*;

class ProposalOrderOrchestrator {
    private static ProposalOrderOrchestrator orch = new ProposalOrderOrchestrator();

    private final GraphStore store = GraphStoreImpl.getInstance();

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
        SnapshotHash snapshotHash = store.snapshot().snapshotHash();

        for(Proposal proposal: proposals) {
            boolean isAccepted = this.isAccepted(snapshotHash, event, proposal);
            if (isAccepted) {
                accepted.add(proposal);
            } else {
                rejected.add(proposal);
            }
        }

        //use acceptance policy to validate mutation.
        //for now use one proposal only randomly
        //as there is no criteria that defines
        //a priority
        List<Proposal> finalAcceptedList = new ArrayList<>();
        List<Proposal> finalRejectedList = new ArrayList<>();

        Proposal finalAccepted = pickDeterministicFirst(accepted);
        Proposal finalRejected = pickDeterministicFirst(rejected);


        if(finalAccepted != null){
            finalAcceptedList.add(finalAccepted);
        }
        if(finalRejected != null){
            finalRejectedList.add(finalRejected);
        }

        proposalMap.put("accepted", finalAcceptedList);
        proposalMap.put("rejected", finalRejectedList);

        return proposalMap;
    }

    private boolean isAccepted(SnapshotHash latestHash, MutationEvent event, Proposal proposal){
        if(event == null || proposal == null || latestHash == null) return false;

        SnapshotHash eventHash = event.getSnapshotHash();
        if(eventHash == null || eventHash.getValue() == null || latestHash.getValue() == null) return false;

        String a = eventHash.getValue().trim();
        String b = latestHash.getValue().trim();

        boolean ok = a.equals(b);
        if(!ok){
            // Console.log("POO_V0_BATCH_STALENESS", "event=" + a + " latest=" + b);
            // or ProposalMonitor.record("poo.batch.staleness", ...)
        }
        return ok;
    }


    private Proposal pickDeterministicFirst(List<Proposal> list){
        if(list == null || list.isEmpty()) return null;

        java.util.List<Proposal> copy = new java.util.ArrayList<>(list);
        copy.sort(java.util.Comparator.comparing(p -> safeKey(p)));
        return copy.get(0);
    }

    private String safeKey(Proposal p){
        if(p == null) return "";
        if(p.getId() != null && !p.getId().trim().isEmpty()) return p.getId().trim();

        // stable-ish fallback (adjust to your fields)
        int ins = (p.getInsert()  == null) ? 0 : p.getInsert().size();
        int upd = (p.getUpdate()  == null) ? 0 : p.getUpdate().size();
        int del = (p.getDelete()  == null) ? 0 : p.getDelete().size();
        int edg = (p.getEdges()   == null) ? 0 : p.getEdges().size();

        return "i" + ins + "|u" + upd + "|d" + del + "|e" + edg;
    }
}
