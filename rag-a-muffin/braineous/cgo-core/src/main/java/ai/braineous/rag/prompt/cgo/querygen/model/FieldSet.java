package ai.braineous.rag.prompt.cgo.querygen.model;

import java.util.List;

public class FieldSet {

    private final List<FieldDefinition> fields;

    public FieldSet(List<FieldDefinition> fields) {
        this.fields = fields;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }
}
