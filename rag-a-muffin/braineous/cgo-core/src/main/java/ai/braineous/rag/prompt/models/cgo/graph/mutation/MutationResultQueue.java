package ai.braineous.rag.prompt.models.cgo.graph.mutation;


public interface MutationResultQueue {

    public void enqueue(MutationResult result);

    public MutationResult dequeueOne();

}
