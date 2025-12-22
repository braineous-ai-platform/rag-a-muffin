package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import java.util.ArrayDeque;
import java.util.Queue;

class MutationResultQueueImpl implements MutationResultQueue{
    private static MutationResultQueueImpl resultQueue = new MutationResultQueueImpl();

    private Queue<MutationResult> events = new ArrayDeque<>();
    private QueueListener queueListener;

    private MutationResultQueueImpl(){
        this.queueListener = MutationResultQueueConsumer.getInstance();
    }

    static MutationResultQueueImpl getInstance(){
        return resultQueue;
    }

    @Override
    public void enqueue(MutationResult result) {
        //orchestor enqueues
        this.events.add(result);

        this.queueListener.signal();
    }

    @Override
    public MutationResult dequeueOne() {
        //event_consumer_dequeeuss
        MutationResult event = this.events.remove();

        return event;
    }
}
