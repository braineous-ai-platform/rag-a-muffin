package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EdgeUpdateSemanticsTests {

    @Test
    void edge_reupsert_does_not_overwrite_text() {
        Console.log("test.start", "edge.update.text_stable");

        GraphBuilder gb = GraphBuilder.getInstance();
        gb.clear();

        Fact aus = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }"); aus.setMode("atomic");
        Fact dfw = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }"); dfw.setMode("atomic");
        gb.addNode(aus); gb.addNode(dfw);

        Fact e1 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\", \"v\":\"v1\" }");
        e1.setMode("relational");
        gb.bind(new Input(aus, dfw, e1), null);

        String beforeText = gb.snapshot().edges().get("Edge:AUS_DFW").getText();

        Fact e2 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\", \"v\":\"v2\" }");
        e2.setMode("relational");
        gb.bind(new Input(aus, dfw, e2), null);

        String afterText = gb.snapshot().edges().get("Edge:AUS_DFW").getText();

        assertEquals(beforeText, afterText, "edge text must remain stable on re-upsert");
    }

    @Test
    void edge_reupsert_merges_attributes() {
        Console.log("test.start", "edge.update.attrs_merge");

        GraphBuilder gb = GraphBuilder.getInstance();
        gb.clear();

        Fact aus = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }"); aus.setMode("atomic");
        Fact dfw = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }"); dfw.setMode("atomic");
        gb.addNode(aus); gb.addNode(dfw);

        Fact e1 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        e1.setMode("relational");
        e1.setAttributes(new java.util.HashSet<>(java.util.Set.of("a")));
        gb.bind(new Input(aus, dfw, e1), null);

        Fact e2 = new Fact("Edge:AUS_DFW", "{ \"id\":\"Edge:AUS_DFW\" }");
        e2.setMode("relational");
        e2.setAttributes(new java.util.HashSet<>(java.util.Set.of("b")));
        gb.bind(new Input(aus, dfw, e2), null);

        var edge = gb.snapshot().edges().get("Edge:AUS_DFW");
        assertNotNull(edge);
        assertNotNull(edge.getAttributes());
        assertTrue(edge.getAttributes().contains("a"));
        assertTrue(edge.getAttributes().contains("b"));
    }

    @Test
    void edge_reupsert_does_not_rewire_endpoints_for_same_id() {
        Console.log("test.start", "edge.update.no_rewire");

        GraphBuilder gb = GraphBuilder.getInstance();
        gb.clear();

        Fact aus = new Fact("Airport:AUS", "{ \"id\":\"Airport:AUS\" }"); aus.setMode("atomic");
        Fact dfw = new Fact("Airport:DFW", "{ \"id\":\"Airport:DFW\" }"); dfw.setMode("atomic");
        Fact ord = new Fact("Airport:ORD", "{ \"id\":\"Airport:ORD\" }"); ord.setMode("atomic");
        gb.addNode(aus); gb.addNode(dfw); gb.addNode(ord);

        Fact e1 = new Fact("Edge:FIXED", "{ \"id\":\"Edge:FIXED\" }");
        e1.setMode("relational");
        gb.bind(new Input(aus, dfw, e1), null);

        var before = gb.snapshot().edges().get("Edge:FIXED");
        assertNotNull(before);
        String beforeFrom = before.getFromFactId();
        String beforeTo = before.getToFactId();

        // attempt to "rewire" same id to different endpoints
        Fact e2 = new Fact("Edge:FIXED", "{ \"id\":\"Edge:FIXED\" }");
        e2.setMode("relational");
        gb.bind(new Input(aus, ord, e2), null);

        var after = gb.snapshot().edges().get("Edge:FIXED");
        assertNotNull(after);

        assertEquals(beforeFrom, after.getFromFactId(), "edge from must not change on re-upsert");
        assertEquals(beforeTo, after.getToFactId(), "edge to must not change on re-upsert");
    }

}
