package ai.braineous.rag.prompt.cgo.prompt;

import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CatalogOrchestratorIT {

    private static final String DB = "cgo";
    private static final String COL = "catalog_orch_it";

    @Inject
    MongoClient mongoClient;

    private CatalogMongoStore store;
    private CatalogOrchestrator orch;

    @BeforeEach
    void setup() {
        Console.log("CATALOG_ORCH_IT/setup", "start");

        mongoClient.getDatabase(DB).getCollection(COL).drop();
        try { Thread.sleep(150L); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

        store = new CatalogMongoStore(mongoClient, DB, COL);
        orch = new CatalogOrchestrator(store);

        Console.log("CATALOG_ORCH_IT/setup", "done");
    }

    @Test
    void upsert_then_getEntry_roundtrip() {
        Console.log("CATALOG_ORCH_IT/upsert_then_get", "start");

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

        Console.log("CATALOG_ORCH_IT/upsert_then_get", "done");
    }

    @Test
    void listEntries_and_snapshot_are_consistent() {
        Console.log("CATALOG_ORCH_IT/list_and_snapshot", "start");

        CatalogEntry a = new CatalogEntry();
        a.setQueryKind("A");
        a.setLlmInstructions(Arrays.asList("x"));

        orch.upsertEntry(a);

        List<CatalogEntry> list = orch.listEntries();
        assertNotNull(list);
        assertEquals(1, list.size());

        CatalogSnapshot snap = orch.resolveSnapshot("A");
        assertNotNull(snap);
        assertNotNull(snap.getByQueryKind());
        assertTrue(snap.getByQueryKind().containsKey("A"));

        Console.log("CATALOG_ORCH_IT/list_and_snapshot", "done");
    }

    @Test
    void clear_wipes_all_entries() {
        Console.log("CATALOG_ORCH_IT/clear", "start");

        CatalogEntry a = new CatalogEntry();
        a.setQueryKind("A");
        a.setLlmInstructions(Arrays.asList("x"));

        orch.upsertEntry(a);
        assertNotNull(orch.getEntry("A"));

        orch.clear();

        assertNull(orch.getEntry("A"));
        assertEquals(0, orch.listEntries().size());

        Console.log("CATALOG_ORCH_IT/clear", "done");
    }
}

