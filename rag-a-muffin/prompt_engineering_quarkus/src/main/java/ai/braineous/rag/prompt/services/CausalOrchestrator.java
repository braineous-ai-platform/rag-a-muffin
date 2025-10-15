package ai.braineous.rag.prompt.services;

import ai.braineous.rag.prompt.models.LLMPrompt;
import ai.braineous.rag.prompt.utils.Console;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CausalOrchestrator {
    
    public void orchestrate(LLMPrompt prompt){
        Console.log("causal_orchestrator", prompt);
    }
}
