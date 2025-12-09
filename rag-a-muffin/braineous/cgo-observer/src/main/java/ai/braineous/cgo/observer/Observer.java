package ai.braineous.cgo.observer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.HistoryView;
import ai.braineous.cgo.history.ScorerResult;

import ai.braineous.rag.prompt.observe.Console;

import java.util.List;
import java.util.Objects;

public class Observer {
    private final HistoryStore store = new HistoryStore();

    /**
     * High-level WHY() entrypoint for now:
     * Given a full HistoryView, compute a simple snapshot:
     *  - how many events
     *  - what was the last score
     *  - what's the average score
     *
     * Later we can add per-queryKind methods, trend windows, bands, etc.
     */
    public WhySnapshot snapshot(HistoryView historyView) {
        Console.log("observer_snapshot_start", historyView);

        if (historyView == null || historyView.size() == 0) {
            Console.log("observer_snapshot_empty", null);
            return WhySnapshot.empty();
        }

        List<HistoryRecord> records = historyView.getRecords();

        int total = records.size();

        // last record
        HistoryRecord lastRecord = records.get(total - 1);
        ScorerResult lastResult = lastRecord.getResult();
        Double lastScore = (lastResult != null) ? lastResult.getScore() : null;

        // average score across all non-null results
        double avg = records.stream()
                .map(HistoryRecord::getResult)
                .filter(Objects::nonNull)
                .mapToDouble(ScorerResult::getScore)
                .average()
                .orElse(Double.NaN);

        Double averageScore = Double.isNaN(avg) ? null : avg;

        WhySnapshot snapshot = new WhySnapshot(total, lastScore, averageScore);
        Console.log("observer_snapshot_result", snapshot);
        return snapshot;
    }

    /**
     * Narrowed WHY() over a single queryKind.
     * This stays as a convenience wrapper and can be extended later.
     */
    public WhySnapshot snapshotForQueryKind(HistoryView historyView, String queryKind) {
        Console.log("observer_snapshot_for_query_kind_start", queryKind);

        if (historyView == null || historyView.size() == 0 || queryKind == null) {
            return WhySnapshot.empty();
        }

        List<HistoryRecord> filtered = store.findHistory(queryKind).getRecords();
        Console.log("observer_filtered_records", filtered);

        if (filtered.isEmpty()) {
            return WhySnapshot.empty();
        }

        int total = filtered.size();

        HistoryRecord lastRecord = filtered.get(total - 1);
        ScorerResult lastResult = lastRecord.getResult();
        Double lastScore = (lastResult != null) ? lastResult.getScore() : null;

        double avg = filtered.stream()
                .map(HistoryRecord::getResult)
                .filter(Objects::nonNull)
                .mapToDouble(ScorerResult::getScore)
                .average()
                .orElse(Double.NaN);

        Double averageScore = Double.isNaN(avg) ? null : avg;

        WhySnapshot snapshot = new WhySnapshot(total, lastScore, averageScore);
        Console.log("observer_snapshot_for_query_kind_result", snapshot);
        return snapshot;
    }
}
