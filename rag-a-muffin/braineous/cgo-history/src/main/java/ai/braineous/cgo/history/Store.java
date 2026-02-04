package ai.braineous.cgo.history;

import java.util.List;

public interface Store {

    public void addRecord(HistoryRecord record);

    public HistoryView findHistory(String queryKind);

    public List<HistoryRecord> getAll();


    public void upsertPending(HistoryRecord record);

    public void markAccepted(String factId, String executionId, String commitId);

    public List<HistoryRecord> findByStatus(String factId, HistoryStatus status);


    public HistoryRecord findLatest(String factId);

}
