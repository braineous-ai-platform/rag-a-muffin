package ai.braineous.rag.prompt.models.cgo;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.services.cgo.RuleExtractor;
import ai.braineous.rag.prompt.services.cgo.causal.CausalRuleExtractor;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class RuleTests {

    @Test
    public void testBuildRuleTopology() throws Exception{
        String jsonStr = Resources.getResource("models/reasoning/excellent_cricket_game/rule.json");
        JsonObject ruleJson = JsonParser.parseString(jsonStr).getAsJsonObject();
        //Console.log("debug", ruleJson);

        JsonArray rulesArray = new JsonArray();
        rulesArray.add(ruleJson);
        //rulesArray.add(ruleJson);
        Console.log("facts", rulesArray);

        String prompt = ruleJson.get("id").getAsString();
        RuleExtractor ruleExtractor = new CausalRuleExtractor();
        List<Rule> rules = ruleExtractor.extract(prompt, rulesArray);

        Console.log("extracted_rules", rules);
    }
}
