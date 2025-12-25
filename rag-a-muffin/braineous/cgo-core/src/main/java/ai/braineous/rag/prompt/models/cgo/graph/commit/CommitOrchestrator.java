package ai.braineous.rag.prompt.models.cgo.graph.commit;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.models.cgo.graph.GraphStore;
import ai.braineous.rag.prompt.models.cgo.graph.GraphStoreImpl;
import ai.braineous.rag.prompt.models.cgo.graph.Proposal;
import ai.braineous.rag.prompt.models.cgo.graph.SnapshotHash;
import ai.braineous.rag.prompt.models.cgo.graph.mutation.MutationResult;

import java.util.List;
import java.util.Set;

public class CommitOrchestrator {

    public CommitResult orchestrate(MutationResult result){
        if (result == null || result.getSnapshotHash() == null || result.getSnapshotHash().getValue() == null) {
            return new CommitResult(false);
        }

        GraphStore store = GraphStoreImpl.getInstance();
        if(store == null){
            //TODO: WHY later
            return new CommitResult(false);
        }

        //make sure store snapshot matches result snapshot
        SnapshotHash resultSnapShot = result.getSnapshotHash();
        SnapshotHash storeSnapShot = store.snapshot().snapshotHash();

        String storeHash = (storeSnapShot == null) ? null : storeSnapShot.getValue();
        String resultHash = (resultSnapShot == null) ? null : resultSnapShot.getValue();

        if (storeHash == null || resultHash == null || !storeHash.equals(resultHash)) {
            // snapshot mismatch → stale proposal (retry)
            return new CommitResult(false);
        }

        //commit the mutation
        boolean commit = this.commit(store, result);
        if(!commit){
            //TODO: WHY later
            return new CommitResult(false);
        }

        //commited
        CommitResult commitResult = new CommitResult();
        commitResult.setMutationResult(result);
        commitResult.setOk(true);

        return commitResult;
    }

    private boolean commit(GraphStore store, MutationResult result) {
        try {
            List<Proposal> accepted = result.getAccepted();
            if (accepted == null || accepted.isEmpty()) return false;
            if (accepted.size() != 1) return false;

            Proposal proposal = accepted.getFirst();
            if (proposal == null) return false;

            Set<Fact> inserts = proposal.getInsert();
            Set<Fact> updates = proposal.getUpdate();
            Set<Fact> deletes = proposal.getDelete();
            Set<Relationship> relationships = proposal.getEdges();

            // --- collect ids ---
            Set<String> deleteIds = new java.util.HashSet<>();
            if (deletes != null) {
                for (Fact f : deletes) {
                    if (f != null && f.getId() != null) {
                        deleteIds.add(f.getId());
                    }
                }
            }

            Set<String> updateIds = new java.util.HashSet<>();
            if (updates != null) {
                for (Fact f : updates) {
                    if (f != null && f.getId() != null) {
                        updateIds.add(f.getId());
                    }
                }
            }

            // --- 1) deletes (highest precedence) ---
            if (deletes != null) {
                for (Fact f : deletes) {
                    if (f == null || f.getId() == null) continue;
                    store.deleteNode(f);
                }
            }

            // --- 2) updates (skip if deleted) ---
            if (updates != null) {
                for (Fact f : updates) {
                    if (f == null || f.getId() == null) continue;
                    if (!deleteIds.contains(f.getId())) {
                        store.upsertNode(f);
                    }
                }
            }

            // --- 3) inserts (skip if deleted OR updated) ---
            if (inserts != null) {
                for (Fact f : inserts) {
                    if (f == null || f.getId() == null) continue;
                    String id = f.getId();
                    if (!deleteIds.contains(id) && !updateIds.contains(id)) {
                        store.upsertNode(f);
                    }
                }
            }

            // --- 4) relationships (skip if endpoints deleted) ---
            if (relationships != null) {
                for (Relationship r : relationships) {
                    if (r == null || r.getFrom() == null || r.getTo() == null) continue;
                    if (r.getFrom().getId() == null || r.getTo().getId() == null) continue;
                    String fromId = r.getFrom().getId();
                    String toId = r.getTo().getId();
                    if (!deleteIds.contains(fromId) && !deleteIds.contains(toId)) {
                        store.mutate(r.getFrom(), r.getTo(), r.getEdge());
                    }
                }
            }

            return true;
        } catch (Exception e) {
            // TODO: report_to_substrate_observer
            return false;
        }
    }

}
