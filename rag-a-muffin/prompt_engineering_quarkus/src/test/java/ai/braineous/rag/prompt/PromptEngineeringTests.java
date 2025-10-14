package ai.braineous.rag.prompt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.utils.Resources;

public class PromptEngineeringTests {

    @Test
    public void testContextModel() throws Exception {
        System.out.println("____testContextModel____");

        String jsonStr = Resources.getResource("models/reasoning/mom_son_relationship/meme.json");
        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();

        System.out.println(json);
    }
}
