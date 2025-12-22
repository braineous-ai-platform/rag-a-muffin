package ai.braineous.rag.prompt.cgo.api;

public class Instruction {

    private String text;

    public Instruction() {}

    public Instruction(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
