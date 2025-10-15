package ai.braineous.rag.prompt;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.Context;
import ai.braineous.rag.prompt.models.InputInstructions;
import ai.braineous.rag.prompt.models.LLMPrompt;
import ai.braineous.rag.prompt.models.OutputInstructions;
import ai.braineous.rag.prompt.models.QueryRelationships;
import ai.braineous.rag.prompt.services.CausalOrchestrator;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class ReasoningIntegrationTests {
    private LLMPrompt llmPrompt;

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private CausalOrchestrator causalOrchestrator = new CausalOrchestrator();

    @Test
    public void testReasoningMemePathAndAnswer() throws Exception {
        JsonObject userPrompt = new JsonObject();
        this.llmPrompt = new LLMPrompt();

        //user_query
        String userQuery = "son, are you bringing a date home for Christmas?";

        //weaviate_embeddings
        String embeddingsStr = Resources.getResource("models/reasoning/mom_son_relationship/weaviate_embeddings.json");
        JsonObject embeddingsJson = JsonParser.parseString(embeddingsStr).getAsJsonObject();
        JsonArray embeddingsArray = embeddingsJson.getAsJsonObject("data")
                                                  .getAsJsonObject("Get")
                                                  .getAsJsonArray("Fact");

        InputInstructions inputInstructions = new InputInstructions(userQuery, embeddingsArray);
        this.llmPrompt.setInput(inputInstructions);

        //parse_user_prompt
        String jsonStr = Resources.getResource("models/reasoning/mom_son_relationship/meme.json");
        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
        userPrompt = json;

        //generate_context
        Context context = this.generateContext(userPrompt);
        this.llmPrompt.setContext(context);

        //set_output_instructions
        String goal = userPrompt.get("goal").getAsString();
        String format = userPrompt.get("output_format").getAsString();
        OutputInstructions outputInstructions = new OutputInstructions(goal, format);
        this.llmPrompt.setOutput(outputInstructions);

        //start_orchestration based on "Causal Orchestration"
        this.causalOrchestrator.orchestrate(llmPrompt);
    }

    private Context generateContext(JsonObject userPrompt){
        Console.log("____generate_context____", userPrompt.toString());
        Context context = new Context();

        JsonArray facts = userPrompt.get("facts").getAsJsonArray();
        JsonArray rules = userPrompt.get("rules").getAsJsonArray();

        QueryRelationships queryRelationships = new QueryRelationships();
        queryRelationships.setFacts(facts);
        queryRelationships.setRules(rules);

        context.setQueryRelationships(queryRelationships);

        return context;
    }
}
