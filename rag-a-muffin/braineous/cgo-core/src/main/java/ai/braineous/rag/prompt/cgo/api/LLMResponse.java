package ai.braineous.rag.prompt.cgo.api;

public class LLMResponse extends CGOBaseModel{

    private LLMRequest llmRequest;

    private String rawResponse;

    private boolean success;

    //---------------------------------------------

    public LLMResponse() {
    }

    //--------------------------------------------


    public LLMRequest getLlmRequest() {
        return llmRequest;
    }

    public void setLlmRequest(LLMRequest llmRequest) {
        this.llmRequest = llmRequest;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
