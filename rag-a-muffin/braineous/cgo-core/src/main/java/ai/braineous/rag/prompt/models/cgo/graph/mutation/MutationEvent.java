package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;

import java.util.HashSet;
import java.util.Set;

class MutationEvent {
    private String id;

    private Set<Proposal> proposals = new HashSet<>();

    public MutationEvent() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Proposal> getProposals() {
        return proposals;
    }

    public void setProposals(Set<Proposal> proposals) {
        this.proposals = proposals;
    }

    @Override
    public String toString() {
        return "MutationEvent{" +
                "id='" + id + '\'' +
                ", proposals=" + proposals +
                '}';
    }
}
