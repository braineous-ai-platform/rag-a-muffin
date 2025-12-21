package ai.braineous.rag.prompt.models.cgo.graph.mutation;


interface MutationQueue {

    public void enqueue(MutationEvent event);

    public MutationEvent dequeueOne();

}
