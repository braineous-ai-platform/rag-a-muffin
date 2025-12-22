package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.data.FNOFactExtractors;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class GraphBuilderTests {

    @BeforeEach
    public void setup(){
        GraphBuilder.getInstance().clear();
    }

    @Test
    public void testSnapshot_SimpleAirportGraph() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

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
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

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
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

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
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

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
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

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
        GraphBuilder graphBuilder = GraphBuilder.getInstance();
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

    @Test
    public void testBindFailsForSelfEdge_NoMutation() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

        // given
        Fact aus = new Fact("Airport:AUS", """
        {"id":"Airport:AUS","kind":"Airport","name":"Austin"}
        """);

        Fact edge = new Fact("Flight:AUS-AUS:001", """
        {"id":"Flight:AUS-AUS:001","kind":"Flight","from":"Airport:AUS","to":"Airport:AUS"}
        """);

        graphBuilder.addNode(aus);
        graphBuilder.addNode(edge);

        GraphSnapshot before = graphBuilder.snapshot();
        Console.log("graph_snapshot_self_edge_before", before);

        assertEquals(2, before.nodes().size());
        assertEquals(0, before.edges().size());

        // when: self-edge bind (from == to)
        Input input = new Input(aus, aus, edge);
        BindResult result = graphBuilder.bind(input, null);

        // then: must fail and not mutate
        assertFalse(result.isOk(), "bind_should_fail_for_self_edge");

        GraphSnapshot after = graphBuilder.snapshot();
        Console.log("graph_snapshot_self_edge_after", after);

        assertEquals(2, after.nodes().size());
        assertEquals(0, after.edges().size());
    }

    @Test
    public void testBindFailsWhenEdgeFactIsNull_NoMutation() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

        // given
        Fact aus = new Fact("Airport:AUS", """
        {"id":"Airport:AUS","kind":"Airport","name":"Austin"}
        """);
        Fact dfw = new Fact("Airport:DFW", """
        {"id":"Airport:DFW","kind":"Airport","name":"Dallas"}
        """);

        graphBuilder.addNode(aus);
        graphBuilder.addNode(dfw);

        GraphSnapshot before = graphBuilder.snapshot();
        Console.log("graph_snapshot_null_edge_before", before);

        assertEquals(2, before.nodes().size());
        assertEquals(0, before.edges().size());

        // when: edgeFact is null
        Input input = new Input(aus, dfw, null);
        BindResult result = graphBuilder.bind(input, null);

        // then: must fail and not mutate
        assertFalse(result.isOk(), "bind_should_fail_when_edge_fact_null");

        GraphSnapshot after = graphBuilder.snapshot();
        Console.log("graph_snapshot_null_edge_after", after);

        assertEquals(2, after.nodes().size());
        assertEquals(0, after.edges().size());
    }

    @Test
    public void testBindFailsWhenInputIsNull_NoMutation() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

        // given
        Fact aus = new Fact("Airport:AUS", """
        {"id":"Airport:AUS","kind":"Airport","name":"Austin"}
        """);
        Fact dfw = new Fact("Airport:DFW", """
        {"id":"Airport:DFW","kind":"Airport","name":"Dallas"}
        """);

        graphBuilder.addNode(aus);
        graphBuilder.addNode(dfw);

        GraphSnapshot before = graphBuilder.snapshot();
        Console.log("graph_snapshot_null_input_before", before);

        assertEquals(2, before.nodes().size());
        assertEquals(0, before.edges().size());

        // when: null Input
        BindResult result = graphBuilder.bind(null, null);

        // then: must fail and not mutate
        assertFalse(result.isOk(), "bind_should_fail_when_input_null");

        GraphSnapshot after = graphBuilder.snapshot();
        Console.log("graph_snapshot_null_input_after", after);

        assertEquals(2, after.nodes().size());
        assertEquals(0, after.edges().size());
    }

    @Test
    public void testBindFailsWhenFromOrToFactIsNull_NoMutation() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

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

        GraphSnapshot before = graphBuilder.snapshot();
        Console.log("graph_snapshot_null_from_or_to_before", before);

        assertEquals(3, before.nodes().size());
        assertEquals(0, before.edges().size());

        // when: from is null
        Input inputFromNull = new Input(null, dfw, flight);
        BindResult resultFromNull = graphBuilder.bind(inputFromNull, null);
        assertFalse(resultFromNull.isOk(), "bind_should_fail_when_from_null");

        // when: to is null
        Input inputToNull = new Input(aus, null, flight);
        BindResult resultToNull = graphBuilder.bind(inputToNull, null);
        assertFalse(resultToNull.isOk(), "bind_should_fail_when_to_null");

        GraphSnapshot after = graphBuilder.snapshot();
        Console.log("graph_snapshot_null_from_or_to_after", after);

        // then: graph must remain unchanged
        assertEquals(3, after.nodes().size());
        assertEquals(0, after.edges().size());
    }

    @Test
    public void testSnapshot_IsDefensiveCopy_SnapshotMapsAreUnmodifiable() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

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

        BindResult bind = graphBuilder.bind(new Input(aus, dfw, flight), null);
        assertTrue(bind.isOk(), "bind_should_succeed");

        GraphSnapshot snap = graphBuilder.snapshot();
        Console.log("graph_snapshot_defensive", snap);

        assertEquals(3, snap.nodes().size());
        assertEquals(1, snap.edges().size());

        // then: snapshot maps must not be directly mutable
        assertThrows(UnsupportedOperationException.class, () -> snap.nodes().clear());
        assertThrows(UnsupportedOperationException.class, () -> snap.edges().clear());

        // and builder remains intact
        GraphSnapshot snap2 = graphBuilder.snapshot();
        Console.log("graph_snapshot_defensive_2", snap2);

        assertEquals(3, snap2.nodes().size());
        assertEquals(1, snap2.edges().size());
    }

    @Test
    public void testSnapshot_IsImmutable_ViewSurvivesBuilderClear() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

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

        BindResult bind = graphBuilder.bind(new Input(aus, dfw, flight), null);
        assertTrue(bind.isOk(), "bind_should_succeed");

        GraphSnapshot snap = graphBuilder.snapshot();
        Console.log("graph_snapshot_before_clear", snap);

        assertEquals(3, snap.nodes().size());
        assertEquals(1, snap.edges().size());

        // when: clear builder
        graphBuilder.clear();

        GraphSnapshot afterClear = graphBuilder.snapshot();
        Console.log("graph_snapshot_after_clear", afterClear);

        assertEquals(0, afterClear.nodes().size());
        assertEquals(0, afterClear.edges().size());

        // then: prior snapshot must remain unchanged
        Console.log("graph_snapshot_prior_view_after_clear", snap);
        assertEquals(3, snap.nodes().size());
        assertEquals(1, snap.edges().size());
    }

}
