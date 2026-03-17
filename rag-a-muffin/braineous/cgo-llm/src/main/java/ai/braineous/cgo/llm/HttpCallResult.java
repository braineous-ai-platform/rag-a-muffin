package ai.braineous.cgo.llm;

public class HttpCallResult {

    private final int statusCode;
    private final String body;

    public HttpCallResult(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "HttpCallResult{" +
                "statusCode=" + statusCode +
                ", body='" + body + '\'' +
                '}';
    }
}