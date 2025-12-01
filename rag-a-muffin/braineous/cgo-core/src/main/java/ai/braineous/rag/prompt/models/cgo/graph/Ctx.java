package ai.braineous.rag.prompt.models.cgo.graph;

import java.util.HashMap;
import java.util.Map;

public class Ctx {

    private final Map<String, Object> context = new HashMap<>();

    public Ctx() {
    }

    public void setValue(String key, Object value){
        this.context.put(key, value);
    }

    public Object getValue(String key){
        return this.context.get(key);
    }

    @Override
    public String toString() {
        return "Ctx{" +
                "context=" + context +
                '}';
    }
}
