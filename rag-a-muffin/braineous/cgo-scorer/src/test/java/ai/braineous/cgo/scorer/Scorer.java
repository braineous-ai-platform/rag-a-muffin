package ai.braineous.cgo.scorer;

import ai.braineous.rag.prompt.cgo.api.QueryExecution;

public class Scorer {
    private final HistoryStore store = new HistoryStore();

    public ScorerResult calculateScore(ScorerContext scorerContext){
        HistoryRecord record = null;
        try {
            //get query_execution
            QueryExecution queryExecution = scorerContext.getQueryExecution();
            record = new HistoryRecord(queryExecution);

            String queryKind = queryExecution.getRequest().getMeta().getQueryKind();

            //get history records
            HistoryView view = store.findHistory(queryKind);

            //calculate_result
            ScorerResult result = calculateResult(view);

            return result;
        }finally {
            if (record != null) {
                store.addRecord(record);
            }
        }
    }

    private ScorerResult calculateResult(HistoryView view){
        ScorerResult result = new ScorerResult();
        return result;
    }
}
