package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;

import java.util.Set;

public interface RejectedMutationQueue {

    public void enqueue(Set<Proposal> proposals);

    public Set<Proposal> dequeue();
}
