package ai.braineous.rag.prompt.models.cgo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReasoningContext {

    private List<Fact> facts;

    private Map<String, Object> state;

    public ReasoningContext(){
        this.facts = new ArrayList<>();
        this.state = new HashMap<>();
    }

    public ReasoningContext(List<Fact> facts, Map<String, Object> state) {
        this.facts = facts;
        this.state = state;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ReasoningContext{");
        sb.append("facts=").append(facts);
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }


}
