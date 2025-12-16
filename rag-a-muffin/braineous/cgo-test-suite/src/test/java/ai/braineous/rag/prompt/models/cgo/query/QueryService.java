package ai.braineous.rag.prompt.models.cgo.query;

import com.google.gson.JsonObject;

public class QueryService {

    public JsonObject query(){
        JsonObject responseJson = new JsonObject();

        //generate query_context
        JsonObject queryJson = new JsonObject();

        QueryContext queryContext = new QueryContext(new Query(queryJson));

        //submit query

        return responseJson;
    }
}
