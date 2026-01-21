package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.observe.Console;

class MutationResultQueueConsumer implements QueueListener{
    private static MutationResultQueueConsumer consumer = new MutationResultQueueConsumer();

    private MutationResultQueueConsumer() {
    }

    static MutationResultQueueConsumer getInstance(){
        return consumer;
    }

    @Override
    public void signal() {
        //consume mutation_result from mutation_result_queue
        MutationResultQueue queue = MutationOrchestrator.getInstance().getResultQueue();
        MutationResult result = queue.dequeueOne();

        //set the result
        MutationResultListenerImpl resultListener = (MutationResultListenerImpl) result.getResultListener();
        resultListener.setMutationResult(result);
    }
}
