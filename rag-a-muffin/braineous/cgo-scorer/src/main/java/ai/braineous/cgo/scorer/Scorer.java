package ai.braineous.cgo.scorer;

import ai.braineous.cgo.history.ScorerResult;
import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;

public class Scorer {


    public ScorerResult calculateScore(ScorerContext scorerContext){
        if(scorerContext == null || scorerContext.getQueryExecution() == null){
            //fail silently
            return null;
        }

        //get query_execution
        QueryExecution queryExecution = scorerContext.getQueryExecution();

        //calculate_result
        ScorerResult result = calculateResult(queryExecution);

        return result;
    }

    private ScorerResult calculateResult(QueryExecution execution){
        ScorerResult result = new ScorerResult();

        ValidationResult prompt = execution.getPromptValidation();
        ValidationResult llm = execution.getLlmResponseValidation();
        ValidationResult domain = execution.getDomainValidation();

        if(prompt == null || llm == null || domain == null){
            result.setScore(0.0d);
            return result;
        }

        //prompt failed, 0.0
        if(!prompt.isOk()){
            result.setScore(0.0d);
            return result;
        }

        //llm failed, 0.33
        if(!llm.isOk()){
            result.setScore(0.33d);
            return result;
        }

        //domain failed, 0.66
        if(!domain.isOk()){
            result.setScore(0.66d);
            return result;
        }

        result.setScore(1.0d);

        return result;
    }
}
