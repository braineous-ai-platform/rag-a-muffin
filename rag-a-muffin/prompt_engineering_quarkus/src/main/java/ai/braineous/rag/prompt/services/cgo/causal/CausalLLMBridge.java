package ai.braineous.rag.prompt.services.cgo.causal;

import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.services.CausalOrchestrator;
import ai.braineous.rag.prompt.cgo.api.LLMBridge;
import ai.braineous.rag.prompt.cgo.api.LLMContext;

public class CausalLLMBridge implements LLMBridge{

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private CausalOrchestrator causalOrchestrator = new CausalOrchestrator();

    @Override
    public GraphView submit(LLMContext llmContext){
        //Console.log("llm_context", llmContext);

        //COG-orchestrate
        return this.causalOrchestrator.orchestrate(llmContext);
    }
}
