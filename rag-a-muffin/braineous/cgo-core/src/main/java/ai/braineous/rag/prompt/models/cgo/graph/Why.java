package ai.braineous.rag.prompt.models.cgo.graph;

import java.util.HashMap;
import java.util.Map;

public class Why {
    private Map<String, String> details = new HashMap<>();

    private SnapshotHash snapshotHash;

    public Why(SnapshotHash snapshotHash) {
        this.snapshotHash = snapshotHash;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public SnapshotHash getSnapshotHash() {
        return snapshotHash;
    }

    public void setSnapshotHash(SnapshotHash snapshotHash) {
        this.snapshotHash = snapshotHash;
    }

    public void addDetail(String label, String detail){
        this.details.put(label, detail);
    }

    @Override
    public String toString() {
        return "Why{" +
                "details=" + details +
                ", snapshotHash=" + snapshotHash +
                '}';
    }
}
