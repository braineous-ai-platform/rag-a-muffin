package ai.braineous.rag.prompt.cgo.api;

public class LlmInstruction {

    private String text;

    public LlmInstruction() {}

    public LlmInstruction(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
