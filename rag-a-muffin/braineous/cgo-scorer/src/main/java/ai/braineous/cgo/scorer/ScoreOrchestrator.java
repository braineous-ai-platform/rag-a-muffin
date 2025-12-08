package ai.braineous.cgo.scorer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;

public class ScoreOrchestrator {
    private final Scorer scorer;
    private final HistoryStore store;

    public ScoreOrchestrator() {
        this(new Scorer(), new HistoryStore());
    }

    // package-private for tests
    ScoreOrchestrator(Scorer scorer, HistoryStore store) {
        this.scorer = scorer;
        this.store = store;
    }


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
            this.storeRecord(record);
        }
    }

    public HistoryStore getStore() {
        return store;
    }

    private void storeRecord(HistoryRecord record){
        try {
            if (record != null) ;
            {
                store.addRecord(record);
            }
        }catch (Exception e){
            //fail-silently
        }
    }
}
