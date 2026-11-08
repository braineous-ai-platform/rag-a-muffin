package ai.braineous.motion.it.jit;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.cgo.api.RelationshipProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JitRelationshipProvider implements RelationshipProvider {

    @Override
    public List<Relationship> provideRelationships(List<Fact> facts) {
        List<Relationship> relationships = new ArrayList<Relationship>();

        if (facts == null || facts.isEmpty()) {
            return relationships;
        }

        Map<String, Fact> factsById = new HashMap<String, Fact>();
        Fact vendor = null;
        Fact sales = null;
        Fact inventory = null;

        for (Fact fact : facts) {
            if (fact == null || fact.getId() == null || fact.getText() == null) {
                continue;
            }

            factsById.put(fact.getId(), fact);

            String kind = this.kindOf(fact);

            if ("Vendor".equals(kind) && fact.getId().startsWith("Vendor:") && vendor == null) {
                vendor = fact;
            }

            if ("Sales".equals(kind) && fact.getId().startsWith("Sales:") && sales == null) {
                sales = fact;
            }

            if ("Inventory".equals(kind) && fact.getId().startsWith("Inventory:") && inventory == null) {
                inventory = fact;
            }
        }

        Set<String> seen = new HashSet<String>();

        this.addReferenced(relationships, seen, vendor, factsById, "product");
        this.addReferenced(relationships, seen, sales, factsById, "product");
        this.addReferenced(relationships, seen, inventory, factsById, "product");
        this.addReferenced(relationships, seen, inventory, factsById, "location");

        return relationships;
    }

    private void addReferenced(
            List<Relationship> relationships,
            Set<String> seen,
            Fact from,
            Map<String, Fact> factsById,
            String fieldName) {

        if (from == null) {
            return;
        }

        String toId = this.referencedFactId(from, fieldName);

        if (toId == null) {
            return;
        }

        Fact to = factsById.get(toId);

        if (to == null) {
            return;
        }

        String fromId = from.getId();

        if (fromId.equals(toId)) {
            return;
        }

        String key = fromId + "->" + toId;

        if (!seen.add(key)) {
            return;
        }

        Edge edge = new Edge();
        edge.setId("Edge:" + key);
        edge.setFromFactId(fromId);
        edge.setToFactId(toId);
        edge.setMode("relational");

        Fact fromRef = new Fact(fromId, from.getText());
        fromRef.setMode("atomic");

        Fact toRef = new Fact(toId, to.getText());
        toRef.setMode("atomic");

        relationships.add(new Relationship(fromRef, toRef, edge));
    }

    private String referencedFactId(Fact fact, String fieldName) {
        JsonObject factJson;

        try {
            factJson = JsonParser.parseString(fact.getText()).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }

        if (!factJson.has(fieldName) || factJson.get(fieldName).isJsonNull()) {
            return null;
        }

        return factJson.get(fieldName).getAsString();
    }

    private String kindOf(Fact fact) {
        JsonObject factJson;

        try {
            factJson = JsonParser.parseString(fact.getText()).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }

        if (!factJson.has("kind") || factJson.get("kind").isJsonNull()) {
            return null;
        }

        return factJson.get("kind").getAsString();
    }
}
