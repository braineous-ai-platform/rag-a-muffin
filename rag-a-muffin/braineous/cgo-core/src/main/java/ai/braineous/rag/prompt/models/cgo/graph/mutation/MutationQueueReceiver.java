package ai.braineous.rag.prompt.models.cgo.graph.mutation;


import java.util.Set;

interface MutationQueueReceiver {

    public void enqueue(MutationEvent event);

    public Set<MutationEvent> dequeue();

}
