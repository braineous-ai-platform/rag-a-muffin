package ai.braineous.cgo.history;

import java.util.ArrayList;
import java.util.List;

public class HistoryStore {
    private static HistoryStore store = new HistoryStore();
    private final List<HistoryRecord> records = new ArrayList<>();

    private HistoryStore() {
    }

    public static HistoryStore getInstance(){
        return HistoryStore.store;
    }
    //---------------------------------------------------------

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

    public List<HistoryRecord> getAll(){
        return this.records;
    }

    public void clear(){
        this.records.clear();
    }
}
