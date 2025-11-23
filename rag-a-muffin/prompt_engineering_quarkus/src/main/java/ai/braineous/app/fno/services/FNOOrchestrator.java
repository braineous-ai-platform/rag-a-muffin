package ai.braineous.app.fno.services;

import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import com.google.gson.JsonArray;

import ai.braineous.rag.prompt.cgo.api.LLMBridge;
import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.services.cgo.causal.CausalLLMBridge;

public class FNOOrchestrator {

    // TODO: [eventually] : make_it_quarkus_containarized.
    // For now no dependency_injection overhead
    // @Inject
    private LLMBridge llmBridge = new CausalLLMBridge();

    public void orchestrate(JsonArray flightsJsonArray) {
        try {
            LLMContext context = new LLMContext();

            FactExtractor factExtractor = new FNOFactExtractor();

            context.build("flights",
                    flightsJsonArray.toString(), factExtractor,
            null, null, null, null);

            // bridge to CGO
            this.llmBridge.submit(context);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
