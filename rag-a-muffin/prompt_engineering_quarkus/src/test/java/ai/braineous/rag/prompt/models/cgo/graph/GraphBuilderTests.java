package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.data.FNOFactExtractors;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class GraphBuilderTests {

    @Test
    public void testSnapshot_SimpleAirportGraph() {
        Validator validator = new Validator();
        ProposalMonitor proposalMonitor = new ProposalMonitor();
        GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);

        // given
        Fact aus = new Fact("Airport:AUS", """
        {"id":"Airport:AUS","kind":"Airport","name":"Austin"}
        """);
        Fact dfw = new Fact("Airport:DFW", """
        {"id":"Airport:DFW","kind":"Airport","name":"Dallas"}
        """);
        Fact flight = new Fact("Flight:AUS-DFW:001", """
        {"id":"Flight:AUS-DFW:001","kind":"Flight","from":"Airport:AUS","to":"Airport:DFW"}
        """);

        graphBuilder.addNode(aus);
        graphBuilder.addNode(dfw);
        graphBuilder.addNode(flight);

        // when
        GraphSnapshot snapshot = graphBuilder.snapshot(); // no rules yet

        // then
        assertNotNull(snapshot);
        assertNotNull(snapshot.id());
        assertEquals(3, snapshot.nodes().size());
        assertEquals(0, snapshot.edges().size());

        // extra safety checks
        assertTrue(
                snapshot.nodes().values().stream().anyMatch(n -> n.getId().equals("Airport:AUS"))
        );
        assertTrue(
                snapshot.nodes().values().stream().anyMatch(n -> n.getId().equals("Airport:DFW"))
        );

        Console.log("graph_snapshot", snapshot);
    }

    @Test
    public void testBindSucceedsForValidFlightBetweenTwoAirports() {
        Validator validator = new Validator();
        ProposalMonitor proposalMonitor = new ProposalMonitor();
        GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);

        // given
        Fact aus = new Fact("Airport:AUS", """
        {"id":"Airport:AUS","kind":"Airport","name":"Austin"}
        """);
        Fact dfw = new Fact("Airport:DFW", """
        {"id":"Airport:DFW","kind":"Airport","name":"Dallas"}
        """);
        Fact flight = new Fact("Flight:AUS-DFW:001", """
        {"id":"Flight:AUS-DFW:001","kind":"Flight","from":"Airport:AUS","to":"Airport:DFW"}
        """);

        graphBuilder.addNode(aus);
        graphBuilder.addNode(dfw);
        graphBuilder.addNode(flight);

        // when
        GraphSnapshot snapshot = graphBuilder.snapshot(); // no rules yet

        //--------assertions-----------------------------------------
        Console.log("graph_snapshot_atomic_only", snapshot);
        assertNotNull(snapshot);
        assertNotNull(snapshot.id());
        assertEquals(3, snapshot.nodes().size());
        assertEquals(0, snapshot.edges().size());


        Input input = new Input(aus, dfw, flight);
        BindResult bindResult = graphBuilder.bind(input, null);
        assertTrue(bindResult.isOk(), "flight_node_must_be_relational");

        snapshot = graphBuilder.snapshot(); // should have an edge

        //-----assertions------------------------------------------------------
        Console.log("graph_snapshot_atomic_and_relational", snapshot);
        assertNotNull(snapshot);
        assertNotNull(snapshot.id());
        assertEquals(3, snapshot.nodes().size());
        assertEquals(1, snapshot.edges().size());

        // extra safety checks
        assertTrue(
                snapshot.nodes().values().stream().anyMatch(n -> n.getId().equals("Airport:AUS"))
        );
        assertTrue(
                snapshot.nodes().values().stream().anyMatch(n -> n.getId().equals("Airport:DFW"))
        );
        assertTrue(
                snapshot.edges().values().stream().anyMatch(n -> n.getId().equals("Flight:AUS-DFW:001"))
        );
    }

    @Test
    public void testBindFailsWhenFlightReferencesMissingAirportTo() {
        Validator validator = new Validator();
        ProposalMonitor proposalMonitor = new ProposalMonitor();
        GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);

        // given: AUS + Flight added, DFW NOT added to the graph
        Fact aus = new Fact("Airport:AUS", """
        {"id":"Airport:AUS","kind":"Airport","name":"Austin"}
        """);
        Fact dfw = new Fact("Airport:DFW", """
        {"id":"Airport:DFW","kind":"Airport","name":"Dallas"}
        """);
        Fact flight = new Fact("Flight:AUS-DFW:001", """
        {"id":"Flight:AUS-DFW:001","kind":"Flight","from":"Airport:AUS","to":"Airport:DFW"}
        """);

        graphBuilder.addNode(aus);
        graphBuilder.addNode(flight);
        // NOTE: no graphBuilder.addNode(dfw);

        GraphSnapshot before = graphBuilder.snapshot();
        assertEquals(2, before.nodes().size());
        assertEquals(0, before.edges().size());

        // when
        Input input = new Input(aus, dfw, flight);
        BindResult bindResult = graphBuilder.bind(input, null);

        // then: should fail, and graph stays unchanged
        assertFalse(bindResult.isOk(), "bind_should_fail_when_airport_missing");

        GraphSnapshot after = graphBuilder.snapshot();
        assertEquals(2, after.nodes().size());
        assertEquals(0, after.edges().size());

        Console.log("graph_snapshot_bind_missing_airport_before", before);
        Console.log("graph_snapshot_bind_missing_airport_after", after);
    }

    @Test
    public void testBindFailsWhenFlightReferencesMissingAirportFrom() {
        Validator validator = new Validator();
        ProposalMonitor proposalMonitor = new ProposalMonitor();
        GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);

        // given: AUS + Flight added, DFW NOT added to the graph
        Fact aus = new Fact("Airport:AUS", """
        {"id":"Airport:AUS","kind":"Airport","name":"Austin"}
        """);
        Fact dfw = new Fact("Airport:DFW", """
        {"id":"Airport:DFW","kind":"Airport","name":"Dallas"}
        """);
        Fact flight = new Fact("Flight:AUS-DFW:001", """
        {"id":"Flight:AUS-DFW:001","kind":"Flight","from":"Airport:AUS","to":"Airport:DFW"}
        """);

        graphBuilder.addNode(dfw);
        graphBuilder.addNode(flight);
        // NOTE: no graphBuilder.addNode(aus);

        GraphSnapshot before = graphBuilder.snapshot();
        assertEquals(2, before.nodes().size());
        assertEquals(0, before.edges().size());

        // when
        Input input = new Input(aus, dfw, flight);
        BindResult bindResult = graphBuilder.bind(input, null);

        // then: should fail, and graph stays unchanged
        assertFalse(bindResult.isOk(), "bind_should_fail_when_airport_missing");

        GraphSnapshot after = graphBuilder.snapshot();
        assertEquals(2, after.nodes().size());
        assertEquals(0, after.edges().size());

        Console.log("graph_snapshot_bind_missing_airport_before", before);
        Console.log("graph_snapshot_bind_missing_airport_after", after);
    }

    @Test
    public void testBindIsIdempotentForSameFlight() {
        Validator validator = new Validator();
        ProposalMonitor proposalMonitor = new ProposalMonitor();
        GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);

        // given
        Fact aus = new Fact("Airport:AUS", """
        {"id":"Airport:AUS","kind":"Airport","name":"Austin"}
        """);
            Fact dfw = new Fact("Airport:DFW", """
        {"id":"Airport:DFW","kind":"Airport","name":"Dallas"}
        """);
            Fact flight = new Fact("Flight:AUS-DFW:001", """
        {"id":"Flight:AUS-DFW:001","kind":"Flight","from":"Airport:AUS","to":"Airport:DFW"}
        """);

        graphBuilder.addNode(aus);
        graphBuilder.addNode(dfw);
        graphBuilder.addNode(flight);

        // when: first bind
        Input input = new Input(aus, dfw, flight);
        BindResult firstBind = graphBuilder.bind(input, null);
        assertTrue(firstBind.isOk(), "first_bind_should_succeed");

        GraphSnapshot afterFirst = graphBuilder.snapshot();
        Console.log("graph_snapshot_after_first_bind", afterFirst);

        assertNotNull(afterFirst);
        assertEquals(3, afterFirst.nodes().size());
        assertEquals(1, afterFirst.edges().size(), "expected_one_edge_after_first_bind");

        // when: second bind with the same input (idempotency check)
        BindResult secondBind = graphBuilder.bind(input, null);
        assertTrue(secondBind.isOk(), "second_bind_should_also_succeed");

        GraphSnapshot afterSecond = graphBuilder.snapshot();
        Console.log("graph_snapshot_after_second_bind", afterSecond);

        // then: graph structure must be unchanged (no duplicate edges)
        assertNotNull(afterSecond);
        assertEquals(3, afterSecond.nodes().size(), "node_count_should_remain_constant");
        assertEquals(1, afterSecond.edges().size(), "edge_count_should_not_increase_for_same_flight");
    }

    //----------------------------------------------------------------------------------
    @Test
    public void testBindSucceedsForValidFlightBetweenTwoAirportsWithValidation() {
        Validator validator = new Validator();
        ProposalMonitor proposalMonitor = new ProposalMonitor();
        GraphBuilder graphBuilder = new GraphBuilder(validator, proposalMonitor);
        Function<Fact, Boolean> validationRule = new FNOFactExtractors.SimpleValidationRuleGenerator();

        // given
        Fact aus = new Fact("Airport:AUS", """
        {"id":"Airport:AUS","kind":"Airport","name":"Austin"}
        """);
        Fact dfw = new Fact("Airport:DFW", """
        {"id":"Airport:DFW","kind":"Airport","name":"Dallas"}
        """);
        Fact flight = new Fact("Flight:AUS-DFW:001", """
        {"id":"Flight:AUS-DFW:001","kind":"Flight","from":"Airport:AUS","to":"Airport:DFW"}
        """);

        aus.setValidationRule(validationRule);
        dfw.setValidationRule(validationRule);
        flight.setValidationRule(validationRule);

        graphBuilder.addNode(aus);
        graphBuilder.addNode(dfw);
        graphBuilder.addNode(flight);

        // when
        GraphSnapshot snapshot = graphBuilder.snapshot(); // no rules yet

        //--------assertions-----------------------------------------
        Console.log("graph_snapshot_atomic_only", snapshot);
        assertNotNull(snapshot);
        assertNotNull(snapshot.id());
        assertEquals(3, snapshot.nodes().size());
        assertEquals(0, snapshot.edges().size());


        Input input = new Input(aus, dfw, flight);
        BindResult bindResult = graphBuilder.bind(input, null);
        assertTrue(bindResult.isOk(), "flight_node_must_be_relational");

        snapshot = graphBuilder.snapshot(); // should have an edge

        //-----assertions------------------------------------------------------
        Console.log("graph_snapshot_atomic_and_relational", snapshot);
        assertNotNull(snapshot);
        assertNotNull(snapshot.id());
        assertEquals(3, snapshot.nodes().size());
        assertEquals(1, snapshot.edges().size());

        // extra safety checks
        assertTrue(
                snapshot.nodes().values().stream().anyMatch(n -> n.getId().equals("Airport:AUS"))
        );
        assertTrue(
                snapshot.nodes().values().stream().anyMatch(n -> n.getId().equals("Airport:DFW"))
        );
        assertTrue(
                snapshot.edges().values().stream().anyMatch(n -> n.getId().equals("Flight:AUS-DFW:001"))
        );
    }
}
