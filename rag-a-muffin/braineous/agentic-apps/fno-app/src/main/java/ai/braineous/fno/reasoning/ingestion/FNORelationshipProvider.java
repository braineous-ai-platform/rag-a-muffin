package ai.braineous.fno.reasoning.ingestion;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.cgo.api.RelationshipProvider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FNORelationshipProvider implements RelationshipProvider {

    @Override
    public List<Relationship> provideRelationships(List<Fact> facts) {
        if (facts == null || facts.isEmpty()) return List.of();

        Map<String, List<Fact>> flightsByFromAirport = new HashMap<>();
        List<Fact> flights = new ArrayList<>();

        for (Fact f : facts) {
            if(f.getMode().equals("relational")) {
                JsonObject flightJson = JsonParser.parseString(f.getText()).getAsJsonObject();
                String fromAirport = flightJson.get("from").getAsString();
                flights.add(f);
                flightsByFromAirport.computeIfAbsent(fromAirport, k -> new ArrayList<>()).add(f);
            }
        }

        if (flights.size() < 2) return List.of();

        List<Relationship> rels = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();

        for (Fact a : flights) {
            JsonObject aJson;
            try {
                aJson = JsonParser.parseString(a.getText()).getAsJsonObject();
            } catch (Exception e) {
                continue;
            }

            // need both keys
            if (!aJson.has("from") || !aJson.has("to")) continue;

            String to = aJson.get("to").getAsString(); // a lands here

            List<Fact> candidates = flightsByFromAirport.getOrDefault(to, List.of()); // flights that depart from hub
            for (Fact b : candidates) {
                if (a.getId().equals(b.getId())) continue;

                String key = a.getId() + "->" + b.getId();
                if (!seen.add(key)) continue;

                Edge edge = new Edge();
                edge.setFromFactId(a.getId());
                edge.setToFactId(b.getId());

                rels.add(new Relationship(a, b, edge));
            }
        }

        return List.copyOf(rels);
    }
}

