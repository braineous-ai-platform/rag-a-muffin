package ai.braineous.rag.prompt.cgo.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Fact {

    private String id;

    private String text;

    private Set<String> attributes = new HashSet<>();

    private String mode = "atomic";

    private Function<Fact, Boolean> validationRule = null;

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

    public Function<Fact, Boolean> getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(Function<Fact, Boolean> validationRule) {
        this.validationRule = validationRule;
    }

    @Override
    public String toString() {
        return "Fact{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", attributes=" + attributes +
                ", mode='" + mode + '\'' +
                ", validationRule=" + validationRule +
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

    //-----------------------
// JSON serialization
//-----------------------

    public String toJsonString() {
        JsonObject o = new JsonObject();

        if (this.id != null) {
            o.addProperty("id", this.id);
        }

        if (this.text != null) {
            o.addProperty("text", this.text);
        }

        if (this.mode != null) {
            o.addProperty("mode", this.mode);
        }

        JsonArray attrs = new JsonArray();
        if (this.attributes != null) {
            for (String a : this.attributes) {
                if (a != null) {
                    attrs.add(a);
                }
            }
        }
        o.add("attributes", attrs);

        return o.toString();
    }

    public static Fact fromJson(JsonObject o) {
        if (o == null) {
            return null;
        }

        Fact f = new Fact();

        if (o.has("id") && !o.get("id").isJsonNull()) {
            f.setId(o.get("id").getAsString());
        }

        if (o.has("text") && !o.get("text").isJsonNull()) {
            f.setText(o.get("text").getAsString());
        }

        if (o.has("mode") && !o.get("mode").isJsonNull()) {
            f.setMode(o.get("mode").getAsString());
        }

        Set<String> attrs = new HashSet<String>();
        if (o.has("attributes") && o.get("attributes").isJsonArray()) {
            JsonArray arr = o.getAsJsonArray("attributes");
            int i = 0;
            while (i < arr.size()) {
                if (!arr.get(i).isJsonNull()) {
                    attrs.add(arr.get(i).getAsString());
                }
                i = i + 1;
            }
        }
        f.setAttributes(attrs);

        // validationRule intentionally NOT restored
        // it is runtime-only, not persistent state

        return f;
    }

}
