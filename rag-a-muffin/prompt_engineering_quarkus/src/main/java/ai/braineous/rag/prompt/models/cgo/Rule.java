package ai.braineous.rag.prompt.models.cgo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Rule {

    private String id;

    private String name;

    private double weight;

    private List<String> lhsFactIds;

    private Function<ReasoningContext, Fact> derive;

    public Rule(){
        this.lhsFactIds = new ArrayList<>();
    }

    public Rule(String id, String name, double weight, List<String> lhsFactIds, Function<ReasoningContext, Fact> derive) {
        this.derive = derive;
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

    @Override
    public String toString() {
        return "Rule [id=" + id + ", name=" + name + ", weight=" + weight + ", lhsFactIds=" + lhsFactIds + ", derive="
                + derive + "]";
    }
}
