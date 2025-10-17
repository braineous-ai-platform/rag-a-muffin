package ai.braineous.rag.prompt.services.cgo.causal;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class CausalFactExtractorTests {

    @Test
    public void testExtract() throws Exception {
        //parse_user_prompt
        String jsonStr = Resources.getResource("models/reasoning/excellent_cricket_game/fact.json");
        JsonObject fact = JsonParser.parseString(jsonStr).getAsJsonObject();

        Console.log("fact", fact);
    }
}
