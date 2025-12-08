package ai.braineous.cgo.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A read-only snapshot of all HistoryRecords.
 *
 * This is NOT a database abstraction.
 * This is simply an in-memory view (typically backed by a List).
 *
 * Scorer will take a HistoryView and:
 *   - filter by queryKind
 *   - maybe look at last N events
 *   - maybe apply time-window logic (future)
 *
 * No interface. No plug points. Just data + minimal helpers.
 */
public final class HistoryView {

    private final List<HistoryRecord> records;

    public HistoryView() {
        records = new ArrayList<>();
    }

    public HistoryView(List<HistoryRecord> records) {
        // defensive copy but shallow — cheap and stable
        this.records = List.copyOf(Objects.requireNonNull(records, "records must not be null"));
    }

    public List<HistoryRecord> getRecords() {
        return records;
    }

    public void addRecord(HistoryRecord record){
        this.records.add(record);
    }

    /**
     * Convenience stream so Scorer can filter fast.
     */
    public Stream<HistoryRecord> stream() {
        return records.stream();
    }

    /**
     * Convenience helper: filter by queryKind.
     * (Since queryKind is the only domain-agnostic WHERE clause.)
     */
    public List<HistoryRecord> filterByQueryKind(String queryKind) {
        return records.stream()
                .filter(r -> r.getQueryKind().equals(queryKind))
                .toList();
    }

    @Override
    public String toString() {
        return "HistoryView{records=" + records.size() + "}";
    }
}

