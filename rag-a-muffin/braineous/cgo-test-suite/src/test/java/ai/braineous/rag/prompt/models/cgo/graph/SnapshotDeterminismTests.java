package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnapshotDeterminismTests {

    @Test
    void snapshotHash_is_same_for_same_nodes_different_insertion_order() {
        Console.log("test.start", "snapshot.determinism.nodes");

        GraphBuilder gb1 = GraphBuilder.getInstance();
        gb1.clear();

        Fact a = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }");
        a.setMode("atomic");
        Fact b = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }");
        b.setMode("atomic");

        gb1.addNode(a);
        gb1.addNode(b);
        String hash1 = gb1.snapshot().snapshotHash().getValue();

        GraphBuilder gb2 = GraphBuilder.getInstance();
        gb2.clear();

        gb2.addNode(b);
        gb2.addNode(a);
        String hash2 = gb2.snapshot().snapshotHash().getValue();

        assertEquals(hash1, hash2,
                "snapshot hash must be deterministic for node insertion order");
    }

    @Test
    void snapshotHash_is_same_for_same_edges_different_insertion_order() {
        Console.log("test.start", "snapshot.determinism.edges");

        GraphBuilder gb1 = GraphBuilder.getInstance();
        gb1.clear();

        Fact aus = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }");
        aus.setMode("atomic");
        Fact dfw = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }");
        dfw.setMode("atomic");

        gb1.addNode(aus);
        gb1.addNode(dfw);

        Fact e1 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        e1.setMode("relational");
        gb1.bind(new Input(aus, dfw, e1), null);

        String hash1 = gb1.snapshot().snapshotHash().getValue();

        GraphBuilder gb2 = GraphBuilder.getInstance();
        gb2.clear();

        gb2.addNode(aus);
        gb2.addNode(dfw);

        Fact e2 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        e2.setMode("relational");
        gb2.bind(new Input(aus, dfw, e2), null);

        String hash2 = gb2.snapshot().snapshotHash().getValue();

        assertEquals(hash1, hash2,
                "snapshot hash must be deterministic for edge insertion order");
    }

    @Test
    void snapshotHash_is_same_for_equivalent_graph_built_in_different_orders() {
        Console.log("test.start", "snapshot.determinism.mixed");

        // Build graph A
        GraphBuilder gb1 = GraphBuilder.getInstance();
        gb1.clear();

        Fact aus1 = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }");
        aus1.setMode("atomic");
        Fact dfw1 = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }");
        dfw1.setMode("atomic");
        Fact edge1 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        edge1.setMode("relational");

        gb1.addNode(aus1);
        gb1.addNode(dfw1);
        gb1.bind(new Input(aus1, dfw1, edge1), null);

        String hash1 = gb1.snapshot().snapshotHash().getValue();

        // Build graph B (different order)
        GraphBuilder gb2 = GraphBuilder.getInstance();
        gb2.clear();

        Fact dfw2 = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }");
        dfw2.setMode("atomic");
        Fact aus2 = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }");
        aus2.setMode("atomic");
        Fact edge2 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        edge2.setMode("relational");

        gb2.addNode(dfw2);
        gb2.addNode(aus2);
        gb2.bind(new Input(aus2, dfw2, edge2), null);

        String hash2 = gb2.snapshot().snapshotHash().getValue();

        assertEquals(hash1, hash2,
                "snapshot hash must be deterministic for equivalent graph states");
    }


}
