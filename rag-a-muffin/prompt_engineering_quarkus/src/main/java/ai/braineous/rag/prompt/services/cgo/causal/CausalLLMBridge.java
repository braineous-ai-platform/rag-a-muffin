package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.List;
import java.util.Set;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.CausalOrchestrator;
import ai.braineous.rag.prompt.services.cgo.LLMBridge;
import ai.braineous.rag.prompt.utils.Console;

public class CausalLLMBridge implements LLMBridge{

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private CausalOrchestrator causalOrchestrator = new CausalOrchestrator();

    @Override
    public void submit(List<Fact> facts, Set<String> rules){
        Console.log("llm_bridge", facts);
        Console.log("llm_bridge", rules);

        //COG-orchestrate
        this.causalOrchestrator.orchestrate(facts, rules);
    }
}
