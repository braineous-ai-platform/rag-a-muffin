package ai.braineous.rag.prompt.models.cgo;

public class RuleTests {

    //TODO: deprecate
    /*@Test
    public void testBuildRuleTopology() throws Exception {
        String jsonStr = Resources.getResource("models/reasoning/excellent_cricket_game/rule.json");
        JsonObject ruleJson = JsonParser.parseString(jsonStr).getAsJsonObject();
        // Console.log("debug", ruleJson);

        JsonArray rulesArray = new JsonArray();
        rulesArray.add(ruleJson);
        // rulesArray.add(ruleJson);
        Console.log("facts", rulesArray);

        String prompt = ruleJson.get("id").getAsString();
        RuleExtractor ruleExtractor = new CausalRuleExtractor();
        List<Rule> rules = ruleExtractor.extract(prompt, rulesArray);

        Console.log("extracted_rules", rules);
    }*/
}
