package ai.braineous.rag.prompt.models;

public class LLMPrompt {

    private InputInstructions input;

    private Context context;

    private OutputInstructions output;

    public LLMPrompt() {
    }

    public InputInstructions getInput() {
        return input;
    }

    public void setInput(InputInstructions input) {
        this.input = input;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public OutputInstructions getOutput() {
        return output;
    }

    public void setOutput(OutputInstructions output) {
        this.output = output;
    }
    
}
