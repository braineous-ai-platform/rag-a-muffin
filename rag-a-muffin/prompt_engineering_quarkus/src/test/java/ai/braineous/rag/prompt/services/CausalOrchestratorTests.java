package ai.braineous.rag.prompt.services;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.utils.Resources;

public class CausalOrchestratorTests {

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private CausalOrchestrator causalOrchestrator = new CausalOrchestrator();


    @Test
    public void testReasoningMemePathAndAnswer() throws Exception {
        //weaviate_embeddings
        String embeddingsStr = Resources.getResource("models/reasoning/mom_son_relationship/weaviate_embeddings.json");
        JsonObject embeddingsJson = JsonParser.parseString(embeddingsStr).getAsJsonObject();
        JsonArray queryEmbeddings = embeddingsJson.getAsJsonObject("data")
                                                  .getAsJsonObject("Get")
                                                  .getAsJsonArray("Fact");


        //parse_user_prompt
        String jsonStr = Resources.getResource("models/reasoning/mom_son_relationship/meme.json");
        JsonObject query = JsonParser.parseString(jsonStr).getAsJsonObject();

        this.causalOrchestrator.orchestrate(queryEmbeddings, query);
    }
}
