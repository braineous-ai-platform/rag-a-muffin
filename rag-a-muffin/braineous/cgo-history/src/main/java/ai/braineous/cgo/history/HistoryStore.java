package ai.braineous.cgo.history;

import java.util.ArrayList;
import java.util.List;

public class HistoryStore {

    private final List<HistoryRecord> records = new ArrayList<>();

    public HistoryStore() {
    }

    public void addRecord(HistoryRecord record) {
        if (record == null) {
            return;
        }
        this.records.add(record);
    }

    public HistoryView findHistory(String queryKind) {
        HistoryView view = new HistoryView();

        if (queryKind == null) {
            return view;
        }

        String needle = queryKind.trim();

        for (HistoryRecord record : this.records) {
            String queryKindLocal = record.getQueryKind();
            if (queryKindLocal != null && queryKindLocal.trim().equals(needle)) {
                view.addRecord(record);
            }
        }

        return view;
    }
}
