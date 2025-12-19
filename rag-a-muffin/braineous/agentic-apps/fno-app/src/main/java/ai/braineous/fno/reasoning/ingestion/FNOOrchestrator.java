package ai.braineous.fno.reasoning.ingestion;

import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.cgo.api.LLMBridge;
import ai.braineous.rag.prompt.cgo.api.LLMContext;
import ai.braineous.rag.prompt.services.cgo.causal.CausalLLMBridge;

import com.google.gson.JsonArray;

public class FNOOrchestrator {
    private LLMBridge llmBridge = new CausalLLMBridge();

    public GraphView orchestrate(JsonArray flightsJsonArray) {
        try {
            LLMContext context = new LLMContext();

            FactExtractor factExtractor = new FNOFactExtractor();

            context.build("flights",
                    flightsJsonArray.toString(),
                    factExtractor,
            null,
                    null,
                    null,
                    null);

            // bridge to CGO
            return this.llmBridge.submit(context);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
