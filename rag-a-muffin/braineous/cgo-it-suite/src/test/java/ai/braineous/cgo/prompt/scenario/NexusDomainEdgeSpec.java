package ai.braineous.cgo.prompt.scenario;

public final class NexusDomainEdgeSpec {

    private final String fromId;
    private final String toId;

    public NexusDomainEdgeSpec(String fromId, String toId) {
        this.fromId = fromId;
        this.toId = toId;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }
}
