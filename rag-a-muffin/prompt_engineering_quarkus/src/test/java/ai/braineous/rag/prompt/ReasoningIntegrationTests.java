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
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class ReasoningIntegrationTests {
    private JsonObject userPrompt;
    private LLMPrompt llmPrompt;

    @Test
    public void testReasoningMemePathAndAnswer() throws Exception {
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
        this.userPrompt = json;

        //generate_context
        Context context = this.generateContext(this.userPrompt);
        this.llmPrompt.setContext(context);

        //set_output_instructions
        String goal = this.userPrompt.get("goal").getAsString();
        String format = this.userPrompt.get("output_format").getAsString();
        OutputInstructions outputInstructions = new OutputInstructions(goal, format);
        this.llmPrompt.setOutput(outputInstructions);

        //out_put_the_prompt_state
        Console.log("llm_prompt", this.llmPrompt);
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
