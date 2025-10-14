package ai.braineous.rag.prompt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.utils.Resources;

public class ReasoningIntegrationTests {

    @Test
    public void testReasoningMemePathAndAnswer() throws Exception {
        System.out.println("____reasoning_meme_path_and_answer____");

        String jsonStr = Resources.getResource("models/reasoning/mom_son_relationship/meme.json");
        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();

        System.out.println(json);
    }
}
