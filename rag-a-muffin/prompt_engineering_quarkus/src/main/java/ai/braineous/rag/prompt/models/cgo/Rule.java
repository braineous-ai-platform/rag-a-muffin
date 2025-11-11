package ai.braineous.rag.prompt.models.cgo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Rule {

    private String id;

    // type of rules such as it's origination like a reference, some media post, etc
    private String type;

    private String name;

    // Rule->(private double weight;) == Fact->(feats[associated_ids_weight])
    // How strongly do these two facts causally co-activate in reasoning space?
    private double weight;

    private String transformer;

    private String instructions;

    private List<String> lhsFactIds;

    private Function<ReasoningContext, Fact> derive;

    public Rule() {
        this.lhsFactIds = new ArrayList<>();
    }

    public Rule(String id, String name, double weight) {
        this.id = id;
        this.name = name;
        this.weight = weight;
    }

    public Rule(String id, String name, String type, String transformer, String instructions,
            double weight, List<String> lhsFactIds, Function<ReasoningContext, Fact> derive) {
        this.derive = derive;
        this.type = type;
        this.transformer = transformer;
        this.instructions = instructions;
        this.id = id;
        this.lhsFactIds = lhsFactIds;
        this.name = name;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public List<String> getLhsFactIds() {
        return lhsFactIds;
    }

    public void setLhsFactIds(List<String> lhsFactIds) {
        this.lhsFactIds = lhsFactIds;
    }

    public Function<ReasoningContext, Fact> getDerive() {
        return derive;
    }

    public void setDerive(Function<ReasoningContext, Fact> derive) {
        this.derive = derive;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTransformer() {
        return transformer;
    }

    public void setTransformer(String transformer) {
        this.transformer = transformer;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    @Override
    public String toString() {
        return "Rule [id=" + id + ", type=" + type + ", name=" + name + ", weight=" + weight + ", transformer="
                + transformer + ", instructions=" + instructions + ", lhsFactIds=" + lhsFactIds + ", derive=" + derive
                + "]";
    }
}
