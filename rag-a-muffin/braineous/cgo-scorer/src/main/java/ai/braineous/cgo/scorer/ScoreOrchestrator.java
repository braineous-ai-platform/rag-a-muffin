package ai.braineous.cgo.scorer;

import ai.braineous.cgo.history.HistoryRecord;
import ai.braineous.cgo.history.HistoryStore;
import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.cgo.history.Store;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ScorerClient;
import ai.braineous.rag.prompt.utils.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ScoreOrchestrator implements ScorerClient {
    private Scorer scorer;

    private Store historyStore;

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
            if (record != null)
            {
                this.historyStore.addRecord(record);
            }
        }catch (Exception e){
            //fail-silently
        }
    }

    //----------------------------------------------------

    private Store findHistoryStore(){
        try {
            if (this.historyStore != null) {
                return this.historyStore;
            }

            synchronized (this) {
                if (this.historyStore != null) {   // <-- add this
                    return this.historyStore;
                }

                //otherwise use the core-cgo-llm-orchestrator
                String pipelineStr = Resources.getResource("pipeline.json");
                JsonObject pipeLineJson = JsonParser.parseString(pipelineStr).getAsJsonObject();

                String storeStr = pipeLineJson.get("history_store").getAsString();
                Store store = (Store) Thread.currentThread().getContextClassLoader().
                        loadClass(storeStr).getDeclaredConstructor().newInstance();
                this.historyStore = store;

                return this.historyStore;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalStateException("Failed to resolve ScorerClient from pipeline.json", e);
        }
    }

}
