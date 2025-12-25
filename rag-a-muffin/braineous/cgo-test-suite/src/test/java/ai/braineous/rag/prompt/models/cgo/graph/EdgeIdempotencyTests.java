package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EdgeIdempotencyTests {

    @Test
    void rebind_same_edge_twice_does_not_change_snapshotHash() {
        Console.log("test.start", "edge.idempotency.hash_stable_on_rebind");

        GraphBuilder gb = GraphBuilder.getInstance();
        gb.clear();

        Fact aus = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }"); aus.setMode("atomic");
        Fact dfw = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }"); dfw.setMode("atomic");
        gb.addNode(aus); gb.addNode(dfw);

        Fact edge = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        edge.setMode("relational");

        // First bind
        BindResult r1 = gb.bind(new Input(aus, dfw, edge), null);
        assertNotNull(r1);
        assertTrue(r1.isOk());

        String hashAfter1 = gb.snapshot().snapshotHash().getValue();

        // Replay bind (same edge id, same endpoints)
        BindResult r2 = gb.bind(new Input(aus, dfw, edge), null);
        assertNotNull(r2);
        assertTrue(r2.isOk());

        String hashAfter2 = gb.snapshot().snapshotHash().getValue();

        assertEquals(hashAfter1, hashAfter2, "rebind replay must not drift snapshot hash");
    }

    @Test
    void rebind_same_edge_unions_attributes_without_duplication() {
        Console.log("test.start", "edge.idempotency.attrs_union");

        GraphBuilder gb = GraphBuilder.getInstance();
        gb.clear();

        Fact aus = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }"); aus.setMode("atomic");
        Fact dfw = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }"); dfw.setMode("atomic");
        gb.addNode(aus); gb.addNode(dfw);

        Fact e1 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        e1.setMode("relational");
        e1.setAttributes(new java.util.HashSet<>(java.util.Set.of("a")));
        assertTrue(gb.bind(new Input(aus, dfw, e1), null).isOk());

        Fact e2 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        e2.setMode("relational");
        e2.setAttributes(new java.util.HashSet<>(java.util.Set.of("a", "b")));
        assertTrue(gb.bind(new Input(aus, dfw, e2), null).isOk());

        Fact e3 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        e3.setMode("relational");
        e3.setAttributes(new java.util.HashSet<>(java.util.Set.of("b")));
        assertTrue(gb.bind(new Input(aus, dfw, e3), null).isOk());

        var edge = gb.snapshot().edges().get("Edge:AUS_DFW");
        assertNotNull(edge);
        assertNotNull(edge.getAttributes());

        // Set semantics: only a and b, nothing else
        assertEquals(2, edge.getAttributes().size(), "attrs must be a set, no duplication drift");
        assertTrue(edge.getAttributes().contains("a"));
        assertTrue(edge.getAttributes().contains("b"));
    }


}
