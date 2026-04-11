package ai.braineous.rag.prompt.cgo.querygen;

import ai.braineous.rag.prompt.cgo.api.Control;
import ai.braineous.rag.prompt.cgo.api.ValidateTask;
import ai.braineous.rag.prompt.cgo.api.ValidationResult;
import ai.braineous.rag.prompt.cgo.query.QueryRequest;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldDefinition;
import ai.braineous.rag.prompt.cgo.querygen.model.FieldGenerationResult;
import com.google.gson.JsonObject;

import java.util.List;

public class TaskFieldGenerator implements FieldGenerator {

    public TaskFieldGenerator() {
    }

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        if (fieldDefinition == null) {
            return false;
        }
        if (fieldDefinition.getName() == null) {
            return false;
        }
        return "task".equals(fieldDefinition.getName());
    }

    @Override
    public FieldGenerationResult generate(FieldDefinition fieldDefinition, QueryRequest request) {
        ValidateTask task = (ValidateTask) request.getTask();
        JsonObject fieldValue = task.toJson();

        JsonObject controlsObject = buildControlsObject(task.getControls());
        fieldValue.add("controls", controlsObject);

        ValidationResult validationResult =
                new ValidationResult(
                        true,
                        "field.task.ok",
                        "VALID",
                        "field_generation",
                        fieldDefinition.getName(),
                        null
                );

        return new FieldGenerationResult(fieldDefinition, fieldValue, validationResult);
    }

    //------------
    private JsonObject buildControlsObject(List<Control> controls) {
        JsonObject controlsObject = new JsonObject();

        if (controls == null) {
            return controlsObject;
        }

        int i = 0;
        while (i < controls.size()) {
            Control control = controls.get(i);

            if (control != null) {
                String key = control.getKey();

                if (key != null) {
                    String value = control.getValue();

                    if (value == null) {
                        controlsObject.add(key, com.google.gson.JsonNull.INSTANCE);
                    } else {
                        controlsObject.addProperty(key, value);
                    }
                }
            }

            i++;
        }

        return controlsObject;
    }
}
