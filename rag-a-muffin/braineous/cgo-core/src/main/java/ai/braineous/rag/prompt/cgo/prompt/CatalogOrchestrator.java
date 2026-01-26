package ai.braineous.rag.prompt.cgo.prompt;

import ai.braineous.rag.prompt.observe.Console;

import java.util.Collections;
import java.util.List;

public class CatalogOrchestrator {

    private CatalogMongoStore store;

    public CatalogOrchestrator() {
        // for CDI / wiring later
    }

    public CatalogOrchestrator(CatalogMongoStore store) {
        if (store == null) {
            throw new IllegalArgumentException("store cannot be null");
        }
        this.store = store;
    }

    public void setStore(CatalogMongoStore store) {
        this.store = store;
    }

    public CatalogEntry getEntry(String queryKind) {
        Console.log("CATALOG_ORCH/getEntry.in", queryKind);

        if (this.store == null) {
            Console.log("CATALOG_ORCH/getEntry.no_store", "store is null");
            return null;
        }

        CatalogEntry e = this.store.getEntry(queryKind);

        Console.log("CATALOG_ORCH/getEntry.out", e == null ? "null" : e.toJsonString());
        return e;
    }

    public void upsertEntry(CatalogEntry entry) {
        Console.log("CATALOG_ORCH/upsertEntry.in", entry == null ? "null" : entry.toJsonString());

        if (this.store == null) {
            Console.log("CATALOG_ORCH/upsertEntry.no_store", "store is null");
            return;
        }

        this.store.upsertEntry(entry);

        Console.log("CATALOG_ORCH/upsertEntry.out", "ok");
    }

    public CatalogSnapshot resolveSnapshot(String queryKind) {
        Console.log("CATALOG_ORCH/resolveSnapshot.in", queryKind);

        if (this.store == null) {
            Console.log("CATALOG_ORCH/resolveSnapshot.no_store", "store is null");
            return null;
        }

        CatalogSnapshot snap = this.store.resolveSnapshot(queryKind);

        Console.log("CATALOG_ORCH/resolveSnapshot.out",
                snap == null ? "null" : snap.toJsonString());
        return snap;
    }

    public List<CatalogEntry> listEntries() {
        Console.log("CATALOG_ORCH/listEntries.in", "go");

        if (this.store == null) {
            Console.log("CATALOG_ORCH/listEntries.no_store", "store is null");
            return Collections.emptyList();
        }

        List<CatalogEntry> list = this.store.listEntries();
        if (list == null) {
            Console.log("CATALOG_ORCH/listEntries.null_list", "store returned null");
            return Collections.emptyList();
        }

        Console.log("CATALOG_ORCH/listEntries.out", "count=" + list.size());
        return list;
    }

    public void clear() {
        Console.log("CATALOG_ORCH/clear.in", "go");

        if (this.store == null) {
            Console.log("CATALOG_ORCH/clear.no_store", "store is null");
            return;
        }

        this.store.clear();

        Console.log("CATALOG_ORCH/clear.out", "ok");
    }
}

