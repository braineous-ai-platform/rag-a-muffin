package ai.braineous.rag.prompt.services.cgo.causal;

import ai.braineous.rag.prompt.services.CausalOrchestrator;
import ai.braineous.rag.prompt.services.cgo.LLMBridge;
import ai.braineous.rag.prompt.services.cgo.LLMContext;
import ai.braineous.rag.prompt.utils.Console;

public class CausalLLMBridge implements LLMBridge{

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private CausalOrchestrator causalOrchestrator = new CausalOrchestrator();

    @Override
    public void submit(LLMContext llmContext){
        Console.log("llm_context", llmContext);

        //COG-orchestrate
        this.causalOrchestrator.orchestrate(llmContext);
    }
}
