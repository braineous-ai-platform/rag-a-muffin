package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.observe.Console;

import java.util.Set;

public class MutationOrchestrator {
    private static MutationOrchestrator orch = new MutationOrchestrator();

    private MutationEventQueue eventQueue = MutationEventQueueImpl.getInstance();
    private MutationResultQueue resultQueue = MutationResultQueueImpl.getInstance();

    private MutationOrchestrator(){

    }

    public static MutationOrchestrator getInstance(){
        return orch;
    }

    public MutationEventQueue getEventQueue() {
        return eventQueue;
    }

    public MutationResultQueue getResultQueue() {
        return resultQueue;
    }

    public ProposalCommitListener orchestrate(Set<Proposal> proposals){

        //create a mutation_event from proposals
        MutationEvent event = new MutationEvent();
        event.setProposals(proposals);

        //submit the event to mutation_event_queue
        this.eventQueue.enqueue(event);

        return null;
    }

    void receiveResultCallback(MutationResult mutationResult){
        Console.log("returned_result", mutationResult);
    }
}
