package ai.braineous.rag.prompt.models.cgo.graph.mutation;

//callbacks to the async caller (GraphBuilder)
public interface MutationResultListener {

    public MutationResult result();
}
