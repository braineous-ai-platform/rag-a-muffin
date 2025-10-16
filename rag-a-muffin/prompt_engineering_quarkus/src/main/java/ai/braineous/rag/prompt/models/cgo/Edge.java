package ai.braineous.rag.prompt.models.cgo;

public class Edge {

    private String ruleId;

    private String fromFactId;

    private String toFactId;

    private double score;

    public Edge(){

    }

    public Edge(String ruleId, String fromFactId, String toFactId, double score) {
        this.fromFactId = fromFactId;
        this.ruleId = ruleId;
        this.score = score;
        this.toFactId = toFactId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
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
        return "Edge [ruleId=" + ruleId + ", fromFactId=" + fromFactId + ", toFactId=" + toFactId + ", score=" + score
                + "]";
    }
}
