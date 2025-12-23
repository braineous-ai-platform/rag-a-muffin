package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.observe.Console;

import java.util.List;
import java.util.Map;

class MutationEventQueueConsumer implements QueueListener{
    private static MutationEventQueueConsumer consumer = new MutationEventQueueConsumer();

    private MutationEventQueueConsumer(){

    }

    static MutationEventQueueConsumer getInstance(){
        return consumer;
    }

    @Override
    public void signal() {
        //consume mutation_event from mutation_event_queue
        MutationEventQueue queue = MutationOrchestrator.getInstance().getEventQueue();
        MutationEvent event = queue.dequeueOne();
        Console.log("event_received", event);

        //ProposalOrderOrchestrator
        ProposalOrderOrchestrator poch = ProposalOrderOrchestrator.getInstance();
        Map<String, List<Proposal>> propMap = poch.prioritize(event.getProposals());

        //derive a mutation_result
        MutationResult result = new MutationResult();
        result.setMutationEventId(event.getId());
        result.setResultListener(event.getResultListener());
        result.setAccepted(propMap.get("accepted"));
        result.setRejected(propMap.get("rejected"));


        //submit a mutation_result to mutation_result_queue
        MutationResultQueue resultQueue = MutationOrchestrator.getInstance().getResultQueue();
        resultQueue.enqueue(result);
    }
}
