package ai.braineous.rag.prompt.models.cgo.graph;

import java.util.ArrayList;
import java.util.List;

public class BindResult {

    private boolean ok = false;

    private Ctx context;

    private List<Error> errors = new ArrayList<>();

    private Why why;

    public BindResult() {

    }

    public BindResult(boolean ok) {
        this.ok = ok;
    }

    public BindResult(boolean ok, Ctx context, List<Error> errors, Why why) {
        this.ok = ok;
        this.context = context;
        this.errors = errors;
        this.why = why;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Ctx getContext() {
        return context;
    }

    public void setContext(Ctx context) {
        this.context = context;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public Why getWhy() {
        return why;
    }

    public void setWhy(Why why) {
        this.why = why;
    }

    @Override
    public String toString() {
        return "BindResult{" +
                "ok=" + ok +
                ", context=" + context +
                ", errors=" + errors +
                ", why=" + why +
                '}';
    }
}
