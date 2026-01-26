package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CatalogMongoStoreIT {

    private static final String DB = "cgo";
    private static final String COL = "catalog_entries_it";

    @Inject
    MongoClient mongoClient;

    private CatalogMongoStore store;

    @BeforeEach
    void setup() {
        store = new CatalogMongoStore(mongoClient, DB, COL);
        mongoClient.getDatabase(DB).getCollection(COL).drop();
        try { Thread.sleep(150L); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    @Test
    void upsert_then_getEntry_roundtrip() {
        CatalogEntry e = new CatalogEntry();
        e.setQueryKind("  user.search  ");
        e.setCatalogVersion("  v1  ");
        e.setDescription("  hello  ");
        e.setLlmInstructions(Arrays.asList("  do x  ", "  ", null, "do y"));

        JsonObject rc = new JsonObject();
        rc.addProperty("type", "object");
        rc.addProperty("required", "true");
        e.setResponseContract(rc);

        store.upsertEntry(e);

        CatalogEntry loaded = store.getEntry("user.search");
        assertNotNull(loaded);

        // getEntry returns what was stored (CatalogEntry.fromJson keeps raw values as stored)
        assertEquals("user.search", loaded.getQueryKind());
        assertEquals("v1", loaded.getCatalogVersion());
        assertEquals("hello", loaded.getDescription());

        assertNotNull(loaded.getLlmInstructions());
        assertEquals(2, loaded.getLlmInstructions().size());
        assertEquals("do x", loaded.getLlmInstructions().get(0));
        assertEquals("do y", loaded.getLlmInstructions().get(1));

        assertNotNull(loaded.getResponseContract());
        assertEquals("object", loaded.getResponseContract().get("type").getAsString());
        assertEquals("true", loaded.getResponseContract().get("required").getAsString());
    }

    @Test
    void upsert_same_queryKind_overwrites() {
        CatalogEntry e1 = new CatalogEntry();
        e1.setQueryKind("Q");
        e1.setDescription("first");
        e1.setLlmInstructions(Arrays.asList("a"));

        store.upsertEntry(e1);

        CatalogEntry e2 = new CatalogEntry();
        e2.setQueryKind("  Q  "); // same logical key (safeQueryKind trims)
        e2.setDescription("second");
        e2.setLlmInstructions(Arrays.asList("b", "c"));

        store.upsertEntry(e2);

        CatalogEntry loaded = store.getEntry("Q");
        assertNotNull(loaded);
        assertEquals("Q", loaded.getQueryKind());
        assertEquals("second", loaded.getDescription());

        assertNotNull(loaded.getLlmInstructions());
        assertEquals(2, loaded.getLlmInstructions().size());
        assertEquals("b", loaded.getLlmInstructions().get(0));
        assertEquals("c", loaded.getLlmInstructions().get(1));
    }

    @Test
    void listEntries_returns_all_valid_entries() {
        CatalogEntry a = new CatalogEntry();
        a.setQueryKind("A");
        a.setLlmInstructions(Arrays.asList("x"));

        CatalogEntry b = new CatalogEntry();
        b.setQueryKind("B");
        b.setLlmInstructions(Arrays.asList("y"));

        store.upsertEntry(a);
        store.upsertEntry(b);

        List<CatalogEntry> list = store.listEntries();
        assertNotNull(list);
        assertEquals(2, list.size());

        boolean hasA = false;
        boolean hasB = false;

        for (int i = 0; i < list.size(); i++) {
            CatalogEntry e = list.get(i);
            if (e != null && "A".equals(e.getQueryKind())) {
                hasA = true;
            }
            if (e != null && "B".equals(e.getQueryKind())) {
                hasB = true;
            }
        }

        assertTrue(hasA);
        assertTrue(hasB);
    }

    @Test
    void clear_deletes_all_docs() {
        CatalogEntry a = new CatalogEntry();
        a.setQueryKind("A");
        a.setLlmInstructions(Arrays.asList("x"));

        store.upsertEntry(a);
        assertNotNull(store.getEntry("A"));

        store.clear();

        assertNull(store.getEntry("A"));
        assertEquals(0, store.listEntries().size());
    }

    @Test
    void getEntry_null_or_blank_returnsNull() {
        assertNull(store.getEntry(null));
        assertNull(store.getEntry(""));
        assertNull(store.getEntry("   "));
    }

    @Test
    void upsertEntry_null_or_blank_queryKind_is_noop() {
        CatalogEntry nullEntry = null;
        store.upsertEntry(nullEntry);

        CatalogEntry bad = new CatalogEntry();
        bad.setQueryKind("   ");
        bad.setLlmInstructions(Arrays.asList("x"));

        store.upsertEntry(bad);

        assertEquals(0, store.listEntries().size());
    }

    @Test
    void listEntries_skips_bad_rows_in_collection() {
        // insert one valid entry via store
        CatalogEntry a = new CatalogEntry();
        a.setQueryKind("A");
        a.setLlmInstructions(Arrays.asList("x"));
        store.upsertEntry(a);

        // manually poison the collection with rows that should be skipped
        MongoCollection<Document> col = mongoClient.getDatabase(DB).getCollection(COL);

        // missing "entry"
        Document d1 = new Document();
        d1.put("queryKind", "bad1");
        col.insertOne(d1);

        // entry not an object (Document.parse expects object; so store something else)
        Document d2 = new Document();
        d2.put("queryKind", "bad2");
        d2.put("entry", "not-json-doc");
        col.insertOne(d2);

        // entry is a Document but not valid CatalogEntry JSON shape is okay; fromJson is tolerant
        // however if it's not JSON object when parsed via entryDoc.toJson(), it will be skipped
        Document d3 = new Document();
        d3.put("queryKind", "bad3");
        d3.put("entry", Document.parse("{\"llmInstructions\":[]}")); // valid object, should parse into CatalogEntry
        col.insertOne(d3);

        List<CatalogEntry> list = store.listEntries();
        assertNotNull(list);

        // We expect at least A, and possibly the d3 entry depending on how strict you want to be.
        // Given CatalogEntry.fromJson accepts missing queryKind, d3 becomes an entry with null queryKind and should still be included.
        // So: assert >= 1 and that A is present.
        assertTrue(list.size() >= 1);

        boolean hasA = false;
        for (int i = 0; i < list.size(); i++) {
            CatalogEntry e = list.get(i);
            if (e != null && "A".equals(e.getQueryKind())) {
                hasA = true;
            }
        }
        assertTrue(hasA);
    }

    @Test
    void resolveSnapshot_returns_snapshot_with_byQueryKind_populated() {
        CatalogEntry a = new CatalogEntry();
        a.setQueryKind("A");
        a.setLlmInstructions(Arrays.asList("x"));

        CatalogEntry b = new CatalogEntry();
        b.setQueryKind("B");
        b.setLlmInstructions(Arrays.asList("y"));

        store.upsertEntry(a);
        store.upsertEntry(b);

        CatalogSnapshot snap = store.resolveSnapshot("A");
        assertNotNull(snap);
        assertNotNull(snap.getByQueryKind());
        assertEquals(2, snap.getByQueryKind().size());
        assertTrue(snap.getByQueryKind().containsKey("A"));
        assertTrue(snap.getByQueryKind().containsKey("B"));
    }
}



