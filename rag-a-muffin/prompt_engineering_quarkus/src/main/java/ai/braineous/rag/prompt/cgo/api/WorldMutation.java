package ai.braineous.rag.prompt.cgo.api;

import java.util.Set;

public class WorldMutation {

    private Set<Fact> insert;     // new facts to introduce
    private Set<Fact> update;     // existing facts with updated payload
    private Set<Fact> delete;     // facts to retire/remove
    private Set<Relationship> edges;  // edge operations (ADD/UPDATE/DELETE)

    public Set<Fact> getInsert() {
        return insert;
    }

    public void setInsert(Set<Fact> insert) {
        this.insert = insert;
    }

    public Set<Fact> getUpdate() {
        return update;
    }

    public void setUpdate(Set<Fact> update) {
        this.update = update;
    }

    public Set<Fact> getDelete() {
        return delete;
    }

    public void setDelete(Set<Fact> delete) {
        this.delete = delete;
    }

    public Set<Relationship> getEdges() {
        return edges;
    }

    public void setEdges(Set<Relationship> edges) {
        this.edges = edges;
    }
}
