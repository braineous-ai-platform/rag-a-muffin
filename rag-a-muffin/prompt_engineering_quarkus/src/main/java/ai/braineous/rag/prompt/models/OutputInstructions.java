package ai.braineous.rag.prompt.models;

public class OutputInstructions {
    private String goal;

    private String format;

    public OutputInstructions() {
    }

    public OutputInstructions(String goal, String format) {
        this.goal = goal;
        this.format = format;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OutputInstructions{");
        sb.append("goal=").append(goal);
        sb.append(", format=").append(format);
        sb.append('}');
        return sb.toString();
    }
}
