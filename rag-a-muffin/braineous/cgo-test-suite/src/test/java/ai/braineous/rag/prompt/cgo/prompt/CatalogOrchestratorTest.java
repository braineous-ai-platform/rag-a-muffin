package ai.braineous.rag.prompt.cgo.prompt;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogOrchestratorTest {

    private static final String DB = "cgo";
    private static final String COL = "catalog_orch_ut";

    private MongoClient mongoClient;
    private CatalogMongoStore store;
    private CatalogOrchestrator orch;

    @BeforeEach
    void setup() {
        Console.log("CATALOG_ORCH_UT/setup", "start");

        mongoClient = MongoClients.create("mongodb://localhost:27017");
        mongoClient.getDatabase(DB).getCollection(COL).drop();

        store = new CatalogMongoStore(mongoClient, DB, COL);
        orch = new CatalogOrchestrator(store);

        Console.log("CATALOG_ORCH_UT/setup", "done");
    }

    @Test
    void upsert_then_getEntry_roundtrip() {
        Console.log("CATALOG_ORCH_UT/upsert_then_get", "start");

        CatalogEntry e = new CatalogEntry();
        e.setQueryKind("  user.search  ");
        e.setCatalogVersion("  v1  ");
        e.setDescription("  hello  ");
        e.setLlmInstructions(Arrays.asList("  do x  ", "  ", null, "do y"));

        JsonObject rc = new JsonObject();
        rc.addProperty("type", "object");
        e.setResponseContract(rc);

        orch.upsertEntry(e);

        CatalogEntry loaded = orch.getEntry("user.search");
        assertNotNull(loaded);
        assertEquals("user.search", loaded.getQueryKind());
        assertEquals("v1", loaded.getCatalogVersion());
        assertEquals("hello", loaded.getDescription());
        assertEquals(2, loaded.getLlmInstructions().size());
        assertEquals("do x", loaded.getLlmInstructions().get(0));
        assertEquals("do y", loaded.getLlmInstructions().get(1));

        Console.log("CATALOG_ORCH_UT/upsert_then_get", "done");
    }

    @Test
    void listEntries_returns_empty_list_not_null() {
        Console.log("CATALOG_ORCH_UT/list_empty", "start");

        List<CatalogEntry> list = orch.listEntries();
        assertNotNull(list);
        assertEquals(0, list.size());

        Console.log("CATALOG_ORCH_UT/list_empty", "done");
    }

    @Test
    void resolveSnapshot_returns_map_with_entries() {
        Console.log("CATALOG_ORCH_UT/resolveSnapshot", "start");

        CatalogEntry a = new CatalogEntry();
        a.setQueryKind("A");
        a.setLlmInstructions(Arrays.asList("x"));

        CatalogEntry b = new CatalogEntry();
        b.setQueryKind("B");
        b.setLlmInstructions(Arrays.asList("y"));

        orch.upsertEntry(a);
        orch.upsertEntry(b);

        CatalogSnapshot snap = orch.resolveSnapshot("A");
        assertNotNull(snap);
        assertNotNull(snap.getByQueryKind());
        assertEquals(2, snap.getByQueryKind().size());
        assertTrue(snap.getByQueryKind().containsKey("A"));
        assertTrue(snap.getByQueryKind().containsKey("B"));

        Console.log("CATALOG_ORCH_UT/resolveSnapshot", "done");
    }

    @Test
    void clear_wipes_all_entries() {
        Console.log("CATALOG_ORCH_UT/clear", "start");

        CatalogEntry a = new CatalogEntry();
        a.setQueryKind("A");
        a.setLlmInstructions(Arrays.asList("x"));

        orch.upsertEntry(a);
        assertNotNull(orch.getEntry("A"));

        orch.clear();

        assertNull(orch.getEntry("A"));
        assertEquals(0, orch.listEntries().size());

        Console.log("CATALOG_ORCH_UT/clear", "done");
    }
}
