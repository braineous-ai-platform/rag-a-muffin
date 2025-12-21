package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import java.util.Set;

class RejectedMutationQueueReceiverImpl implements MutationQueueReceiver{
    @Override
    public void enqueue(MutationEvent event) {

    }

    @Override
    public Set<MutationEvent> dequeue() {
        return null;
    }
}
