package ai.braineous.cgo.scorer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ScorerClient;

public class ScoreOrchestrator implements ScorerClient {
    private Scorer scorer;

    public ScoreOrchestrator() {
        this(new Scorer());
    }

    // package-private for tests
    ScoreOrchestrator(Scorer scorer) {
        this.scorer = scorer;
    }


    @Override
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

    private void storeRecord(HistoryRecord record){
        try {
            if (record != null) ;
            {
                HistoryStore.getInstance().addRecord(record);
            }
        }catch (Exception e){
            //fail-silently
        }
    }
}
