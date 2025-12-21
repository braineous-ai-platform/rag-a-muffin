package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import java.util.ArrayDeque;
import java.util.Queue;

class MutationEventQueueImpl implements MutationEventQueue {
    private static MutationEventQueueImpl mutationEventQueue = new MutationEventQueueImpl();

    private Queue<MutationEvent> events = new ArrayDeque<>();

    private QueueListener queueListener;

    private MutationEventQueueImpl(){
        queueListener = MutationEventQueueConsumer.getInstance();
    }

    static MutationEventQueueImpl getInstance(){
        return MutationEventQueueImpl.mutationEventQueue;
    }

    @Override
    public void enqueue(MutationEvent event) {
        //orchestor enqueues
        this.events.add(event);

        this.queueListener.signal();
    }

    @Override
    public MutationEvent dequeueOne() {

        //event_consumer_dequeeuss
        MutationEvent event = this.events.remove();

        return event;
    }
}
