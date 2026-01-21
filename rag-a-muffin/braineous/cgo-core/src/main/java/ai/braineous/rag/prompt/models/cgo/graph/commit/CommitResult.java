package ai.braineous.rag.prompt.models.cgo.graph.commit;


import ai.braineous.rag.prompt.models.cgo.graph.Why;
import ai.braineous.rag.prompt.models.cgo.graph.mutation.MutationResult;

public class CommitResult {
    // field init helper (thread-safe)
    private static final java.util.concurrent.atomic.AtomicLong EVENT_SEQ =
            new java.util.concurrent.atomic.AtomicLong();
    ;

    private final String id;
    private MutationResult mutationResult;

    private boolean ok = false;

    private Why why;

    public CommitResult() {
        long seq = nextSeq();
        this.id = nextEventId(seq);
    }

    public CommitResult(boolean ok) {
        this();
        this.ok = ok;
    }

    public String getId() {
        return id;
    }

    public MutationResult getMutationResult() {
        return mutationResult;
    }

    public void setMutationResult(MutationResult mutationResult) {
        if (mutationResult == null) return;
        this.mutationResult = mutationResult;
    }


    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Why getWhy() {
        return why;
    }

    public void setWhy(Why why) {
        if (why == null) return;
        this.why = why;
    }

    // call inside constructor
    private static long nextSeq() {
        return EVENT_SEQ.incrementAndGet();
    }

    // call inside constructor
    private static String nextEventId(long seq) {
        // readable + sortable-ish (by seq)
        return "MR-" + seq;
    }

    @Override
    public String toString() {
        String snap = null;
        int acc = -1;
        int rej = -1;

        if (mutationResult != null) {
            if (mutationResult.getSnapshotHash() != null) {
                snap = mutationResult.getSnapshotHash().getValue();
            }
            if (mutationResult.getAccepted() != null) acc = mutationResult.getAccepted().size();
            if (mutationResult.getRejected() != null) rej = mutationResult.getRejected().size();
        }

        return "CommitResult{" +
                "id='" + id + '\'' +
                ", ok=" + ok +
                ", snapshotHash=" + (snap == null ? "null" : snap) +
                ", accepted=" + acc +
                ", rejected=" + rej +
                ", why=" + why +
                '}';
    }
}
