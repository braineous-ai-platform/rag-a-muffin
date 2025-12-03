package ai.braineous.rag.prompt.models.cgo.graph;

public class Error {
    private String code;

    private String message;

    private String hint;

    public Error() {
    }

    public Error(String code, String message, String hint) {
        this.code = code;
        this.message = message;
        this.hint = hint;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", hint='" + hint + '\'' +
                '}';
    }
}
