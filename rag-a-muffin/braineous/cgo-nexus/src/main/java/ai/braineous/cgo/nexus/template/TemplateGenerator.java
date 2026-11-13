package ai.braineous.cgo.nexus.template;

import java.math.BigDecimal;
import java.util.Map;

public class TemplateGenerator {

    public String generate(String definition, BigDecimal result) {
        return definition
                + "\n\ncontext\nThe result is "
                + result.toPlainString()
                + ".";
    }

    public String generate(
            String definition,
            Map<String, String> values) {
        StringBuilder output = new StringBuilder();
        output.append(definition);
        output.append("\n\ncontext");
        if (values != null) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                output.append("\n");
                output.append(entry.getKey());
                output.append(": ");
                output.append(entry.getValue());
            }
        }
        return output.toString();
    }
}
