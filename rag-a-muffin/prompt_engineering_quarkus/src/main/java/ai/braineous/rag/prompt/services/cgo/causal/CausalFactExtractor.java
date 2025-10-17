package ai.braineous.rag.prompt.services.cgo.causal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
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
            Map<String, Object> featsMap;

            JsonObject factJson = factsArray.get(i).getAsJsonObject();

            String id = prompt + ":" + i;
            String text = factJson.get("text").getAsString();
            JsonObject featsJson = factJson.getAsJsonObject("feats");

            //get feats
            Gson gson = new Gson();
            featsMap = gson.fromJson(featsJson, Map.class);

            fact.setId(id);
            fact.setText(text);
            fact.setFeats(featsMap);

            facts.add(fact);
        }

        return facts;
    }

}
