package ai.braineous.rag.prompt.models.cgo.graph.mutation;


public interface MutationEventQueue {

    public void enqueue(MutationEvent event);

    public MutationEvent dequeueOne();

}
