package ai.braineous.rag.prompt.models.cgo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
}
