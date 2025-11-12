package ai.braineous.rag.prompt.services.cgo.causal;

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
