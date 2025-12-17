package ai.braineous.fno.reasoning.observer;

import ai.braineous.cgo.history.HistoryView;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FNOObserverTests {

    @Test
    void observer_returnsHistoryView_forQueryKind() {
        // arrange
        String queryKind = "FNO_VALIDATE";
        Console.log("test.observer.queryKind", queryKind);

        FNOObserver observer = new FNOObserver();

        // act
        HistoryView view = observer.getHistory(queryKind);

        // assert (spine test)
        assertNotNull(view);

        // debug
        Console.log("test.historyview.type", view.getClass().getName());
        Console.log("test.historyview.string", String.valueOf(view));
    }

}
