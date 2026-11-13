package ai.braineous.cgo.nexus.template;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.Input;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NexusTemplateObservationSupport {

    public Fact fact(String id, String text) {
        Fact fact = new Fact(id, text);
        fact.setMode("atomic");
        return fact;
    }

    public Input edgeInput(Fact from, Fact to) {
        Edge edge = new Edge();
        edge.setId("Edge:" + from.getId() + "->" + to.getId());
        edge.setFromFactId(from.getId());
        edge.setToFactId(to.getId());
        edge.setMode("relational");
        return new Input(from, to, edge);
    }

    public String loadResource(String name) {
        InputStream stream = NexusTemplateObservationSupport.class.getResourceAsStream(name);
        if (stream == null) {
            throw new IllegalStateException("missing resource " + name);
        }
        try {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            try {
                stream.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void observe(String generated, String reference) {
        System.out.print("=== GENERATED TEMPLATE BEGIN ===\n");
        System.out.print(generated);
        System.out.print("\n=== GENERATED TEMPLATE END ===\n");
        System.out.print("=== NEXUS REFERENCE TEMPLATE BEGIN ===\n");
        System.out.print(reference);
        System.out.print("\n=== NEXUS REFERENCE TEMPLATE END ===\n");
        System.out.print("=== EQUALITY ===\n");
        System.out.print(String.valueOf(generated.equals(reference)));
        System.out.print("\n");
        assertEquals(reference, generated);
    }

    public void observeTitleAndContext(String generated, String reference) {
        System.out.print("=== GENERATED TEMPLATE BEGIN ===\n");
        System.out.print(generated);
        System.out.print("\n=== GENERATED TEMPLATE END ===\n");
        System.out.print("=== NEXUS REFERENCE TEMPLATE BEGIN ===\n");
        System.out.print(reference);
        System.out.print("\n=== NEXUS REFERENCE TEMPLATE END ===\n");
        boolean equal = this.titleOf(generated).equals(this.titleOf(reference))
                && this.contextMap(generated).equals(this.contextMap(reference));
        System.out.print("=== EQUALITY ===\n");
        System.out.print(String.valueOf(equal));
        System.out.print("\n");
        assertEquals(this.titleOf(reference), this.titleOf(generated));
        assertEquals(this.contextMap(reference), this.contextMap(generated));
    }

    public String titleOf(String template) {
        int split = template.indexOf("\n\n");
        if (split < 0) {
            return template;
        }
        return template.substring(0, split);
    }

    public Map<String, String> contextMap(String template) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        int split = template.indexOf("\n\n");
        if (split < 0) {
            return values;
        }
        String context = template.substring(split + 2);
        if (context.startsWith("context\n")) {
            context = context.substring("context\n".length());
        } else if (context.equals("context")) {
            return values;
        }
        String[] lines = context.split("\n");
        int index = 0;
        while (index < lines.length) {
            String line = lines[index];
            int separator = line.indexOf(": ");
            if (separator > 0) {
                values.put(line.substring(0, separator), line.substring(separator + 2));
            }
            index = index + 1;
        }
        return values;
    }

    public void observeBlocked(String reason, String reference) {
        System.out.print("=== BLOCKED ===\n");
        System.out.print(reason);
        System.out.print("\n");
        System.out.print("=== GENERATED TEMPLATE BEGIN ===\n");
        System.out.print("\n=== GENERATED TEMPLATE END ===\n");
        System.out.print("=== NEXUS REFERENCE TEMPLATE BEGIN ===\n");
        System.out.print(reference);
        System.out.print("\n=== NEXUS REFERENCE TEMPLATE END ===\n");
        System.out.print("=== EQUALITY ===\n");
        System.out.print("blocked\n");
    }
}
