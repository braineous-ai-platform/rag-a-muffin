package ai.braineous.rag.prompt.models.cgo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Fact {

    private String id;

    private String text;

    private Map<String, Object> feats;

    private String mode;

    public Fact(){
        this.feats = new HashMap<>();
    }

    

    public Fact(String id, String text, Map<String, Object> feats) {
        this.id = id;
        this.text = text;
        this.feats = feats;
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

    public Map<String, Object> getFeats() {
        return feats;
    }

    public void setFeats(Map<String, Object> feats) {
        this.feats = feats;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "Fact{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", feats=" + feats +
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
