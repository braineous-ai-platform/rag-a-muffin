package ai.braineous.rag.prompt.services;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class NanoLLMProviderTests {

    @Test
    public void testLLMFlow() throws Exception {
        Console.log("llm_flow_dataset", null);

        //sample_dataset - flights
        String flightsJsonStr = Resources.getResource("models/fno/nano_llm_sample_dataset/flights.json");
        JsonObject flightsJson = JsonParser.parseString(flightsJsonStr).getAsJsonObject();
        Console.log("filgts_json", flightsJson);

         //sample_dataset - disruptions
        String disruptionsJsonStr = Resources.getResource("models/fno/nano_llm_sample_dataset/disruptions.json");
        JsonObject disruptionsJson = JsonParser.parseString(disruptionsJsonStr).getAsJsonObject();
        Console.log("disruptions_json", disruptionsJson);

        //generate flight_facts

        //generate disruption_facts

        //generate rules based on (flight + disruption) facts

        //generate a network subgraph using the facts + rules

        //generate PromptContext using (contraints = demand)

        //get candidate_itineraries from LLM invocation

        //scheme + validation -> final_result (flight_plan)
    }

}
