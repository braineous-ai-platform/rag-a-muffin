package ai.braineous.cgo.nexus.orchestrator;

import ai.braineous.cgo.nexus.arithmetic.ArithmeticEngine;
import ai.braineous.cgo.nexus.map.MapEngine;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.cgo.nexus.template.TemplateGenerator;
import ai.braineous.rag.prompt.cgo.api.Fact;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class NexusOrchestrator {

    private final GraphResolver graphResolver;
    private final ArithmeticEngine arithmeticEngine;
    private final MapEngine mapEngine;
    private final TemplateGenerator templateGenerator;

    public NexusOrchestrator() {
        this.graphResolver = new GraphResolver();
        this.arithmeticEngine = new ArithmeticEngine();
        this.mapEngine = new MapEngine();
        this.templateGenerator = new TemplateGenerator();
    }

    public String execute(
            Fact axis,
            List<String> relatedFacts,
            String expression,
            String definition) {
        ResolvedGraph resolved = this.graphResolver.resolve(axis, relatedFacts);
        if (expression != null && !expression.isBlank()) {
            BigDecimal result = this.arithmeticEngine.evaluate(expression, resolved);
            return this.templateGenerator.generate(definition, result);
        } else {
            Map<String, String> result = this.mapEngine.evaluate(resolved);
            return this.templateGenerator.generate(definition, result);
        }
    }
}
