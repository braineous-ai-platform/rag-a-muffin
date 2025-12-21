package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;

import java.util.Set;

public interface MutationQueue {

    public void enqueue(Set<Proposal> proposals);

}
