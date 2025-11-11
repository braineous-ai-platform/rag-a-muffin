package ai.braineous.rag.prompt.models.cgo.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.models.cgo.Rule;
import ai.braineous.rag.prompt.utils.Console;

public class LLMGraphTests {

    @Test
    public void testArrivalSpokeToHub() throws Exception {
        Console.log("testAddFactsAndEdges", "2");

        // -------------------- Ruleset (from ____causal_orchestrate_rule____)
        // --------------------
        Map<String, Rule> rules = new HashMap<>();

        rules.put("R_airport_node_AUS", new Rule("R_airport_node_AUS",
                "Create airport node.", 0.8));
        rules.put("R_airport_node_DFW", new Rule("R_airport_node_DFW",
                "Create airport node.", 0.8));
        rules.put("R_flight_edge", new Rule("R_flight_edge",
                "Create flight edge from Flight facts.", 1e-9));

        assertEquals(3, rules.size(), "Should have 3 rules loaded");
        assertEquals(1.0, rules.get("R_flight_edge").getWeight(), 1.0);

        // -------------------- Facts (dedup airports; flights unique)
        // ----------------------------
        Fact AUS = new Fact("Airport:AUS", "Airport(AUS, 'AUS')", null);
        Fact DFW = new Fact("Airport:DFW", "Airport(DFW, 'DFW')", null);

        Fact F100 = new Fact("Flight:F100",
                "Flight(id:'F100', AUS, DFW, '2025-10-22T10:00:00Z', '2025-10-22T11:10:00Z')", null);
        Fact F101 = new Fact("Flight:F101",
                "Flight(id:'F101', AUS, DFW, '2025-10-22T11:30:00Z', '2025-10-22T12:40:00Z')", null);

        // -------------------- Build graph
        // -------------------------------------------------------
        LLMGraph g = new LLMGraph();

        List<Fact> airports = List.of(AUS, DFW);
        List<Fact> flights = List.of(F100, F101);
        airports.forEach(g::addFact);
        flights.forEach(g::addFact);

        // flight rule used for both depart/arrive edges; attribute distinguishes
        // direction
        Rule flightRule = rules.get("R_flight_edge");

        Edge departAttrs = new Edge(Set.of("fly", "depart"));
        Edge f100Attrs = new Edge(Set.of("fly", "arrive"));
        Edge f101Attrs = new Edge(Set.of("fly", "arrive"));

        // wire legs: Airport -> Flight (depart), Flight -> Airport (arrive)
        addLeg(g, AUS, F100, DFW, flightRule, departAttrs, f100Attrs);
        addLeg(g, AUS, F101, DFW, flightRule, departAttrs, f101Attrs);

        // -------------------- Assertions: counts
        // -----------------------------------------------
        assertEquals(4, g.network.nodes().size(), "2 airports + 2 flights");
        assertEquals(4, g.network.edges().size(), "2 edges per flight");

        // -------------------- Connectivity spot checks
        // -----------------------------------------
        assertTrue(g.network.predecessors(F100).contains(AUS), "F100 should have AUS as predecessor (depart)");
        assertTrue(g.network.successors(F100).contains(DFW), "F100 should connect to DFW (arrive)");
        assertTrue(g.network.predecessors(F101).contains(AUS));
        assertTrue(g.network.successors(F101).contains(DFW));

        // -------------------- Rule/attribute capture
        // -------------------------------------------
        assertTrue(g.edgeAttrs.containsKey("R_flight_edge"), "edgeAttrs keyed by rule id");
        Set<String> attrs = g.edgeAttrs.get("R_flight_edge");
        assertTrue(attrs.contains("fly"), "should record 'fly' attribute");
        assertTrue(attrs.containsAll(Set.of("fly", "depart", "arrive")));

        // note: since you store Set<String> per rule id, either 'depart' or 'arrive'
        // will appear depending on last insert
        // quick guard: at least one of them must be present
        assertTrue(attrs.contains("depart") || attrs.contains("arrive"));

        // Optional: nodeAttrs observed (may be null feats)
        assertTrue(g.nodeAttrs.containsKey("Airport:AUS"));
        assertTrue(g.nodeAttrs.containsKey("Flight:F100"));
    }

    private static void addLeg(
            LLMGraph g,
            Fact originAirport,
            Fact flight,
            Fact destAirport,
            Rule flightRule,
            Edge departAttrs,
            Edge arriveAttrs) {

        // Airport -> Flight (depart)
        g.addEdge(originAirport, flight, new RuleEdge(flightRule, departAttrs));

        // Flight -> Airport (arrive)
        g.addEdge(flight, destAirport, new RuleEdge(flightRule, arriveAttrs));
    }

    @Test
    public void testFactToGraphGeneration() throws Exception {
        // -------------------- Ruleset --------------------
        Map<String, Rule> rules = new HashMap<>();
        rules.put("R_airport_node", new Rule("R_airport_node", "Create airport node.", 0.8));
        rules.put("R_flight_edge", new Rule("R_flight_edge", "Create flight edge from Flight facts.", 1.0));

        String ausJsonStr = "{\n" + //
                "  \"id\": \"Airport:AUS\",\n" + //
                "  \"kind\": \"Airport\",\n" + //
                "  \"mode\": \"atomic\",\n" + //
                "  \"feats\": {\n" + //
                "    \"code\": \"AUS\",\n" + //
                "    \"name\": \"Austin-Bergstrom International Airport\",\n" + //
                "    \"city\": \"Austin\",\n" + //
                "    \"state\": \"TX\",\n" + //
                "    \"country\": \"USA\",\n" + //
                "    \"tz\": \"America/Chicago\"\n" + //
                "  },\n" + //
                "  \"meta\": {\n" + //
                "    \"source\": \"seed\",\n" + //
                "    \"batchId\": \"b1\",\n" + //
                "    \"observedAt\": \"2025-11-08T18:20:00Z\"\n" + //
                "  }\n" + //
                "}\n" + //
                "";
        JsonObject ausJson = JsonParser.parseString(ausJsonStr).getAsJsonObject();
        Console.log("aus_json", ausJson);

        String dfwJsonStr = "{\n" + //
                "  \"id\": \"Airport:DFW\",\n" + //
                "  \"kind\": \"Airport\",\n" + //
                "  \"mode\": \"atomic\",\n" + //
                "  \"feats\": {\n" + //
                "    \"code\": \"DFW\",\n" + //
                "    \"name\": \"Dallas/Fort Worth International Airport\",\n" + //
                "    \"city\": \"Dallas\",\n" + //
                "    \"state\": \"TX\",\n" + //
                "    \"country\": \"USA\",\n" + //
                "    \"tz\": \"America/Chicago\"\n" + //
                "  },\n" + //
                "  \"meta\": {\n" + //
                "    \"source\": \"seed\",\n" + //
                "    \"batchId\": \"b1\",\n" + //
                "    \"observedAt\": \"2025-11-08T18:20:00Z\"\n" + //
                "  }\n" + //
                "}\n" + //
                "";
        JsonObject dfwJson = JsonParser.parseString(dfwJsonStr).getAsJsonObject();
        Console.log("dfw_json", dfwJson);

        String flightJsonStr = "{\n" + //
                "  \"id\": \"Flight:F120\",\n" + //
                "  \"kind\": \"Flight\",\n" + //
                "  \"mode\": \"relational\",\n" + //
                "  \"slots\": { \"origin\": \"Airport:AUS\", \"dest\": \"Airport:DFW\" },\n" + //
                "  \"feats\": { \"depUtc\": \"2025-10-22T11:20:00Z\", \"arrUtc\": \"2025-10-22T12:25:00Z\" },\n" + //
                "  \"meta\": { \"source\": \"ops\", \"batchId\": \"b1\", \"observedAt\": \"2025-10-22T10:00:00Z\" }\n" + //
                "}\n" + //
                "";
        JsonObject flightJson = JsonParser.parseString(flightJsonStr).getAsJsonObject();
        Console.log("flight_json", flightJson);

        // -------------------- Convert to Facts --------------------
        Fact AUS = new Fact(ausJson.get("id").getAsString(), ausJson.toString(), null);
        Fact DFW = new Fact(dfwJson.get("id").getAsString(), dfwJson.toString(), null);
        Fact F120 = new Fact(flightJson.get("id").getAsString(), flightJson.toString(), null);

        // -------------------- Build graph --------------------
        LLMGraph g = new LLMGraph();
        g.addFact(AUS);
        g.addFact(DFW);
        g.addFact(F120);

        // -------------------- Wire edge from relational fact --------------------
        Rule flightRule = rules.get("R_flight_edge");
        Edge departAttrs = new Edge(Set.of("fly", "depart"));
        Edge arriveAttrs = new Edge(Set.of("fly", "arrive"));

        addLeg(g, AUS, F120, DFW, flightRule, departAttrs, arriveAttrs);

        // -------------------- Assertions --------------------
        assertEquals(3, g.network.nodes().size(), "2 airports + 1 flight");
        assertEquals(2, g.network.edges().size(), "depart + arrive edges");

        assertTrue(g.network.predecessors(F120).contains(AUS), "AUS → F120 (depart)");
        assertTrue(g.network.successors(F120).contains(DFW), "F120 → DFW (arrive)");

        assertTrue(g.edgeAttrs.containsKey("R_flight_edge"));
        Set<String> attrs = g.edgeAttrs.get("R_flight_edge");
        assertTrue(attrs.containsAll(Set.of("fly", "depart", "arrive")));

        assertTrue(g.nodeAttrs.containsKey("Airport:AUS"));
        assertTrue(g.nodeAttrs.containsKey("Flight:F120"));
    }
}
