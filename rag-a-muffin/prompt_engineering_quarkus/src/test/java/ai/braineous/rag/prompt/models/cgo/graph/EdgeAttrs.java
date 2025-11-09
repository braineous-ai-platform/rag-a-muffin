package ai.braineous.rag.prompt.models.cgo.graph;

import java.util.HashSet;
import java.util.Set;

public class EdgeAttrs {
    private Set<String> attributes = new HashSet<>();

    public EdgeAttrs() {
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "EdgeAttrs [attributes=" + attributes + "]";
    }

    public void addAttribute(String attribute) {
        this.attributes.add(attribute);
    }

    public void removeAttribute(String attribute) {
        this.attributes.remove(attribute);
    }
}
