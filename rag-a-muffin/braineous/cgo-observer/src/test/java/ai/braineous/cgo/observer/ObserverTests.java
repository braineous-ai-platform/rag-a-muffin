package ai.braineous.cgo.observer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.HistoryView;
import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.observe.Console;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObserverTests {

    @Test
    void snapshot_withMultipleRecords_shouldComputeLastAndAverageScore() {
        Console.log("test_start", "snapshot_withMultipleRecords_shouldComputeLastAndAverageScore");

        HistoryStore store = new HistoryStore();

        // fake executions just for wiring; real fields don't matter here
        QueryExecution<?> exec1 = null;
        QueryExecution<?> exec2 = null;
        QueryExecution<?> exec3 = null;

        store.addRecord(new HistoryRecord(exec1, new ScorerResult(0.0)));
        store.addRecord(new HistoryRecord(exec2, new ScorerResult(0.5)));
        store.addRecord(new HistoryRecord(exec3, new ScorerResult(1.0)));

        HistoryView view = new HistoryView(store.getAll());

        Observer observer = new Observer();

        WhySnapshot snapshot = observer.snapshot(view);
        Console.log("observer_snapshot", snapshot);

        assertNotNull(snapshot);
        assertEquals(3, snapshot.getTotalEvents());
        assertEquals(1.0, snapshot.getLastScore(), 1e-9);
        assertEquals(0.5, snapshot.getAverageScore(), 1e-9);
    }
}