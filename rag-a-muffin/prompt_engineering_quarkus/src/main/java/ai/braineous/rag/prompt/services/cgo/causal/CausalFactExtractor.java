package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.FactExtractor;

public class CausalFactExtractor implements FactExtractor{

    @Override
    public List<Fact> extract(String prompt, JsonArray factsArray) {
        List<Fact> facts = new ArrayList<>();


        for(int i=0; i<factsArray.size(); i++){
            Fact fact = new Fact();
            Map<String, Object> featsMap = new HashMap<>();

            JsonObject factJson = factsArray.get(i).getAsJsonObject();

            String id = prompt + ":" + i;
            String text = factJson.get("text").getAsString();

            //get feats

            fact.setId(id);
            fact.setText(text);

            facts.add(fact);
        }

        return facts;
    }

}
