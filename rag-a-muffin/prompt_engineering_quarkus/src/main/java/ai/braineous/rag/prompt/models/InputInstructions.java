package ai.braineous.rag.prompt.models;

import com.google.gson.JsonArray;

public class InputInstructions {

    private String userQuery;
    
    private JsonArray embeddings;

    public InputInstructions() {
    }

    public InputInstructions(String userQuery, JsonArray embeddings) {
        this.userQuery = userQuery;
        this.embeddings = embeddings;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InputInstructions{");
        sb.append("userQuery=").append(userQuery);
        sb.append(", embeddings=").append(embeddings);
        sb.append('}');
        return sb.toString();
    }


}
