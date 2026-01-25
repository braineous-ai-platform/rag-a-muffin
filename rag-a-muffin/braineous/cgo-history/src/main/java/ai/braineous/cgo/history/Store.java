package ai.braineous.cgo.history;

import java.util.List;

public interface Store {

    public void addRecord(HistoryRecord record);

    public HistoryView findHistory(String queryKind);

    public List<HistoryRecord> getAll();
}
