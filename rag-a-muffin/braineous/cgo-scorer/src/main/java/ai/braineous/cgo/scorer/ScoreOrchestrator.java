package ai.braineous.cgo.scorer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;

public class ScoreOrchestrator {
    private final Scorer scorer = new Scorer();
    private final HistoryStore store = new HistoryStore();


    public void orchestrate(QueryExecution queryExecution){
        if(queryExecution == null){
            //fail-silently;
            return;
        }

        ScorerContext ctx = new ScorerContext(queryExecution);
        HistoryRecord record = null;
        try{
           ScorerResult result = scorer.calculateScore(ctx);
           if(result != null) {
               record = new HistoryRecord(queryExecution, result);
           }
        }finally {
            if(record != null);{
                store.addRecord(record);
            }
        }
    }
}
