package ai.braineous.rag.prompt.models;

import com.google.gson.JsonArray;

public class InputInstructions {

    private String userQuery;
    
    private JsonArray embeddings;

    public InputInstructions() {
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }

    public JsonArray getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(JsonArray embeddings) {
        this.embeddings = embeddings;
    }
}
