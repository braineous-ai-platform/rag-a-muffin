package ai.braineous.cgo.scorer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ScorerClient;

public class ScoreOrchestrator implements ScorerClient {
    private Scorer scorer;

    private HistoryStore historyStore;

    public ScoreOrchestrator() {
        this(new Scorer());
    }

    // package-private for tests
    ScoreOrchestrator(Scorer scorer) {
        this.scorer = scorer;
        this.historyStore = this.findHistoryStore();
    }

    void setHistoryStore(HistoryStore historyStore) {
        this.historyStore = historyStore;
    }

    @Override
    public void orchestrate(QueryExecution queryExecution){
        if(queryExecution == null){
            //fail-silently;
            return;
        }

        if(queryExecution.isInMemoryMode()){
            this.historyStore = HistoryStore.getInstance();
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
                this.historyStore.addRecord(record);
            }
        }catch (Exception e){
            //fail-silently
        }
    }

    //----------------------------------------------------

    private HistoryStore findHistoryStore(){

        return null;
    }

}
