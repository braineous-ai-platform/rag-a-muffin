package ai.braineous.rag.prompt.models.cgo;

import java.util.*;

public class Fact {

    private String id;

    private String text;

    private Set<String> attributes = new HashSet<>();

    private String mode = "atomic";

    public Fact(){

    }


    public Fact(String id, String text, Set<String> attributes, String mode) {
        this.id = id;
        this.text = text;
        this.attributes = attributes;
        this.mode = mode;
    }

    public Fact(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public Fact(String text) {
        this.text = text;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void addAttribute(String attribute) {
        this.attributes.add(attribute);
    }

    public void removeAttribute(String attribute) {
        this.attributes.remove(attribute);
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "Fact{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", attributes=" + attributes +
                ", mode='" + mode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof Fact)) {
            return false;
        }

        Fact fact = (Fact) o;
        return id.equals(fact.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
