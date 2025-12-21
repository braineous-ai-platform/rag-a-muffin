package ai.braineous.rag.prompt.models.cgo.graph.mutation;


interface MutationResultQueue {

    public void enqueue(MutationResult result);

    public MutationResult dequeueOne();

}
