package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.FactExtractor;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class CausalFactExtractorTests {

    //TDOD: deprecate
    /*@Test
    public void testExtract() throws Exception {
        //parse_user_prompt
        String jsonStr = Resources.getResource("models/reasoning/excellent_cricket_game/fact.json");
        JsonObject fact = JsonParser.parseString(jsonStr).getAsJsonObject();

        JsonArray factsArray = new JsonArray();
        factsArray.add(fact);
        factsArray.add(fact);
        Console.log("facts", factsArray);

        String prompt = fact.get("id").getAsString();
        FactExtractor factExtractor = new FactExtractor();
        List<Fact> facts = factExtractor.extract(prompt);

        Console.log("extracted_facts", facts);
    }*/
}
