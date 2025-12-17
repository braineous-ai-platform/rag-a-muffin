package ai.braineous.fno.reasoning.observer;

import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.HistoryView;

public class FNOObserver {

    public HistoryView getHistory(String queryKind) {
        if (queryKind == null || queryKind.isBlank()) {
            throw new IllegalArgumentException("queryKind must be non-empty");
        }
        return HistoryStore.getInstance().findHistory(queryKind);
    }
}
