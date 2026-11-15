package ai.braineous.cgo.prompt.scenario;

public final class NexusDomainFactSpec {

    private final String id;
    private final String text;

    public NexusDomainFactSpec(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
