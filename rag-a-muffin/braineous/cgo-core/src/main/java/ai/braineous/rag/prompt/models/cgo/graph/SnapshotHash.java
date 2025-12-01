package ai.braineous.rag.prompt.models.cgo.graph;

public class SnapshotHash {
    private String value;

    public SnapshotHash(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SnapshotHash{" +
                "value='" + value + '\'' +
                '}';
    }
}
