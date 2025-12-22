package ai.braineous.cgo.observer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.HistoryView;
import ai.braineous.cgo.history.ScorerResult;

import ai.braineous.rag.prompt.observe.Console;

import java.util.List;
import java.util.Objects;

public class Observer {
    private final HistoryStore store = HistoryStore.getInstance();

    /**
     * Narrowed WHY() over a single queryKind.
     * This stays as a convenience wrapper and can be extended later.
     */
    public WhySnapshot snapshotForQueryKind(String queryKind) {
        Console.log("observer_snapshot_for_query_kind_start", queryKind);

        if (queryKind == null || queryKind.isBlank()) {
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
                .map(ScorerResult::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);

        Double averageScore = Double.isNaN(avg) ? null : avg;

        WhySnapshot snapshot = new WhySnapshot(total, lastScore, averageScore);
        Console.log("observer_snapshot_for_query_kind_result", snapshot);
        return snapshot;
    }
}
