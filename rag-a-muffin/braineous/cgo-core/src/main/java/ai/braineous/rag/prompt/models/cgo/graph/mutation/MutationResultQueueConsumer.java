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
        Console.log("result_received", result);

        //any_processing

        //submit a mutation_result to mutation_orchestrator
        MutationOrchestrator.getInstance().receiveResultCallback(result);
    }
}
