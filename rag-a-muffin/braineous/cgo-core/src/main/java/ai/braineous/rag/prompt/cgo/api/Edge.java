package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class Edge extends Fact {

    private String fromFactId;

    private String toFactId;

    private double score;

    public Edge() {
    }

    public String getFromFactId() {
        return fromFactId;
    }

    public void setFromFactId(String fromFactId) {
        this.fromFactId = fromFactId;
    }

    public String getToFactId() {
        return toFactId;
    }

    public void setToFactId(String toFactId) {
        this.toFactId = toFactId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "fromFactId='" + fromFactId + '\'' +
                ", toFactId='" + toFactId + '\'' +
                ", score=" + score +
                '}';
    }

    //-----------------------
// JSON serialization
//-----------------------

    @Override
    public String toJsonString() {
        JsonObject o = new JsonObject();

        // ---- Fact fields ----
        if (getId() != null) {
            o.addProperty("id", getId());
        }

        if (getText() != null) {
            o.addProperty("text", getText());
        }

        if (getMode() != null) {
            o.addProperty("mode", getMode());
        }

        JsonArray attrs = new JsonArray();
        if (getAttributes() != null) {
            for (String a : getAttributes()) {
                if (a != null) {
                    attrs.add(a);
                }
            }
        }
        o.add("attributes", attrs);

        // ---- Edge fields ----
        if (this.fromFactId != null) {
            o.addProperty("fromFactId", this.fromFactId);
        }

        if (this.toFactId != null) {
            o.addProperty("toFactId", this.toFactId);
        }

        o.addProperty("score", this.score);

        return o.toString();
    }

    public static Edge fromJson(JsonObject o) {
        if (o == null) {
            return null;
        }

        Edge e = new Edge();

        // ---- Fact fields ----
        if (o.has("id") && !o.get("id").isJsonNull()) {
            e.setId(o.get("id").getAsString());
        }

        if (o.has("text") && !o.get("text").isJsonNull()) {
            e.setText(o.get("text").getAsString());
        }

        if (o.has("mode") && !o.get("mode").isJsonNull()) {
            e.setMode(o.get("mode").getAsString());
        }

        Set<String> attrs = new HashSet<String>();
        if (o.has("attributes") && o.get("attributes").isJsonArray()) {
            JsonArray arr = o.getAsJsonArray("attributes");
            int i = 0;
            while (i < arr.size()) {
                if (!arr.get(i).isJsonNull()) {
                    attrs.add(arr.get(i).getAsString());
                }
                i = i + 1;
            }
        }
        e.setAttributes(attrs);

        // ---- Edge fields ----
        if (o.has("fromFactId") && !o.get("fromFactId").isJsonNull()) {
            e.setFromFactId(o.get("fromFactId").getAsString());
        }

        if (o.has("toFactId") && !o.get("toFactId").isJsonNull()) {
            e.setToFactId(o.get("toFactId").getAsString());
        }

        if (o.has("score") && !o.get("score").isJsonNull()) {
            e.setScore(o.get("score").getAsDouble());
        } else {
            e.setScore(1.0); // deterministic default
        }

        return e;
    }
}
