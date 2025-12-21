package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;

import java.util.ArrayList;
import java.util.List;

public class MutationResult {
    private String id;

    private String mutationEventId;

    private List<Proposal> accepted = new ArrayList<>();

    private List<Proposal> rejected = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Proposal> getAccepted() {
        return accepted;
    }

    public void setAccepted(List<Proposal> accepted) {
        this.accepted = accepted;
    }

    public List<Proposal> getRejected() {
        return rejected;
    }

    public void setRejected(List<Proposal> rejected) {
        this.rejected = rejected;
    }

    public String getMutationEventId() {
        return mutationEventId;
    }

    public void setMutationEventId(String mutationEventId) {
        this.mutationEventId = mutationEventId;
    }

    @Override
    public String toString() {
        return "MutationResult{" +
                "id='" + id + '\'' +
                ", mutationEventId='" + mutationEventId + '\'' +
                ", accepted=" + accepted +
                ", rejected=" + rejected +
                '}';
    }
}
