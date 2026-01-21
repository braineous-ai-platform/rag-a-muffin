package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Input;
import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.models.cgo.graph.Rulepack;
import ai.braineous.rag.prompt.models.cgo.graph.SnapshotHash;

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

    public MutationResultListener orchestrate(SnapshotHash snapshotHash,
                                              Input input,
                                              Rulepack rulepack,
                                              Set<Proposal> proposals){
        MutationResultListener resultListener = new MutationResultListenerImpl();

        //create a mutation_event from proposals
        MutationEvent event = new MutationEvent();
        event.setProposals(proposals);
        event.setSnapshotHash(snapshotHash);
        event.setResultListener(resultListener);
        event.setInput(input);
        event.setRulepack(rulepack);

        //submit the event to mutation_event_queue
        this.eventQueue.enqueue(event);

        return resultListener;
    }
}
