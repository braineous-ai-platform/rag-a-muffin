package ai.braineous.rag.prompt.cgo.query;

import java.util.List;

public final class Node {

    public enum Mode {
        ATOMIC,
        RELATIONAL
    }

    private final String id;          // e.g. "Flight:F100"
    private final String text;        // JSON payload as string
    private final List<String> attributes;
    private final Mode mode;

    public Node(String id, String text, List<String> attributes, Mode mode) {
        this.id = id;
        this.text = text;
        this.attributes = List.copyOf(attributes);
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public Mode getMode() {
        return mode;
    }
}
