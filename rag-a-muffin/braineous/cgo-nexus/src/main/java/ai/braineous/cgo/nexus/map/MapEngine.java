package ai.braineous.cgo.nexus.map;

import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapEngine {

    public Map<String, String> evaluate(ResolvedGraph graph) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        if (graph == null) {
            return values;
        }

        GraphSnapshot snapshot = graph.getSnapshot();
        if (snapshot == null || snapshot.nodes() == null) {
            return values;
        }

        for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            if (fact == null || fact.getText() == null) {
                continue;
            }

            JsonObject json = JsonParser.parseString(fact.getText()).getAsJsonObject();
            for (Map.Entry<String, JsonElement> field : json.entrySet()) {
                String name = field.getKey();
                if ("id".equals(name) || "kind".equals(name) || "mode".equals(name)) {
                    continue;
                }

                JsonElement element = field.getValue();
                if (element == null || !element.isJsonPrimitive()) {
                    continue;
                }

                values.put(name, element.getAsString());
            }
        }

        return values;
    }
}
