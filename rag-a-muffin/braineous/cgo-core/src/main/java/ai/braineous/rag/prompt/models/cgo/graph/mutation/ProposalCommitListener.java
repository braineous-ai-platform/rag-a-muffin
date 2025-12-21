package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;

import java.util.List;

//callbacks to the async caller (GraphBuilder)
public interface ProposalCommitListener {

    public MutationResult result();

    public boolean ack();
}
