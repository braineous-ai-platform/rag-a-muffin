package ai.braineous.rag.prompt.cgo.querygen.model;

public class FieldValue {

    private final String fieldName;
    private final String value;

    public FieldValue(String fieldName, String value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getValue() {
        return value;
    }
}
