package ai.braineous.app.fno.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;

public class FNORuleProducer {

    //TODO: unstub...start_here
    public Set<String> produce(List<Fact> facts) throws Exception{
        Console.log("flight_facts", facts);

        String jsonStr = Resources.getResource("models/fno/rules_fno.json");
        Console.log("jsonStr", jsonStr);

        Set<String> rules = new HashSet<>();
        for(Fact fact: facts){
            Console.log("debug", fact);

            JsonObject ruleJson = new JsonObject();

            //id
            String id = "R_airport_node";
            ruleJson.addProperty("id", id);

            //note
            String note = "Create an airport graph node from Airport(code, name) facts.";
            ruleJson.addProperty("note", note);

            //when_then for Functional rendering

            //when
            String when = "[\"Airport($code, $name)\"]";
            ruleJson.addProperty("when", when);

            //then
            String then = "[\n" + //
                                "        {\n" + //
                                "          \"emit\": \"GraphNode($code, 'airport', $name)\"\n" + //
                                "        }\n" + //
                                "      ]";
            ruleJson.addProperty("then", then);

            //weight
            String weight = "0.8";
            ruleJson.addProperty("weight", weight);

            rules.add(ruleJson.toString());
        }
        

        Console.log("generated_rules", rules);

        return rules;
    }
}
