package ai.braineous.rag.prompt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.Context;
import ai.braineous.rag.prompt.models.LLMPrompt;
import ai.braineous.rag.prompt.models.QueryRelationships;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class ReasoningIntegrationTests {
    private JsonObject userPrompt;
    private LLMPrompt llmPrompt;

    @Test
    public void testReasoningMemePathAndAnswer() throws Exception {
        this.llmPrompt = new LLMPrompt();

        //parse_user_prompt
        String jsonStr = Resources.getResource("models/reasoning/mom_son_relationship/meme.json");
        this.parse(jsonStr);

        //generate_context
        Context context = this.generateContext(this.userPrompt);
        llmPrompt.setContext(context);
        Console.log("llm_prompt", this.llmPrompt);
    }

    private void parse(String jsonStr){
        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
        this.userPrompt = json;
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
