package ai.braineous.rag.prompt.models.cgo.graph.mutation;

import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class MutationOrchestratorTests {

    @Test
    public void testOrchestrateFlow() throws Exception{
        Console.log("mutation_event_orchestrator", "testOrchestrateFlow");

        Set<Proposal> proposals = new HashSet<>();
        MutationOrchestrator orch = MutationOrchestrator.getInstance();

        orch.orchestrate(proposals);
        orch.orchestrate(proposals);
    }
}
