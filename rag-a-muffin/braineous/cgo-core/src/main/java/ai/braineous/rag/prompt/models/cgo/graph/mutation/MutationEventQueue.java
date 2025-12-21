package ai.braineous.rag.prompt.models.cgo.graph.mutation;


interface MutationEventQueue {

    public void enqueue(MutationEvent event);

    public MutationEvent dequeueOne();

}
