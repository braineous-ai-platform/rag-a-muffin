package ai.braineous.rag.prompt.models.cgo;

import java.util.HashMap;
import java.util.Map;

public class Fact {

    private String id;

    private String text;

    private Map<String, Object> feats;

    public Fact(){
        this.feats = new HashMap<>();
    }

    public Fact(String id, String text, Map<String, Object> feats) {
        this.id = id;
        this.text = text;
        this.feats = feats;
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

    @Override
    public String toString() {
        return "Fact [id=" + id + ", text=" + text + ", feats=" + feats + "]";
    }

}
