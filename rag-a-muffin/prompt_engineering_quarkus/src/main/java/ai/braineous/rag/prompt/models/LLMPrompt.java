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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LLMPrompt{");
        sb.append("output=").append(output);
        sb.append(", input=").append(input);
        sb.append(", context=").append(context);
        sb.append('}');
        return sb.toString();
    }
    
    
}
