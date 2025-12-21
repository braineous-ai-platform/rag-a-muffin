package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;

import java.util.List;
import java.util.Set;

//callbacks to the async caller (GraphBuilder)
public interface ProposalCommitListener {

    public List<Proposal> committed();

    public List<Proposal> rejected();

    public boolean ack();
}
