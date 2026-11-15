package ai.braineous.cgo.prompt.scenario;

import java.util.ArrayList;
import java.util.List;

public final class NexusDomainScenario {

    private final String name;
    private final String axisId;
    private final List<String> relatedFacts;
    private final String expression;
    private final String definition;
    private final String ruleSection;
    private final String decisionSection;
    private final String expectedOutcome;
    private final String expectedNexusMarker;
    private final List<NexusDomainFactSpec> facts;
    private final List<NexusDomainEdgeSpec> edges;

    public NexusDomainScenario(
            String name,
            String axisId,
            List<String> relatedFacts,
            String expression,
            String definition,
            String ruleSection,
            String decisionSection,
            String expectedOutcome,
            String expectedNexusMarker,
            List<NexusDomainFactSpec> facts,
            List<NexusDomainEdgeSpec> edges) {
        this.name = name;
        this.axisId = axisId;
        this.relatedFacts = relatedFacts;
        this.expression = expression;
        this.definition = definition;
        this.ruleSection = ruleSection;
        this.decisionSection = decisionSection;
        this.expectedOutcome = expectedOutcome;
        this.expectedNexusMarker = expectedNexusMarker;
        this.facts = facts;
        this.edges = edges;
    }

    public String getName() {
        return name;
    }

    public String getAxisId() {
        return axisId;
    }

    public List<String> getRelatedFacts() {
        return relatedFacts;
    }

    public String getExpression() {
        return expression;
    }

    public String getDefinition() {
        return definition;
    }

    public String getRuleSection() {
        return ruleSection;
    }

    public String getDecisionSection() {
        return decisionSection;
    }

    public String getExpectedOutcome() {
        return expectedOutcome;
    }

    public String getExpectedNexusMarker() {
        return expectedNexusMarker;
    }

    public List<NexusDomainFactSpec> getFacts() {
        return facts;
    }

    public List<NexusDomainEdgeSpec> getEdges() {
        return edges;
    }

    public static List<NexusDomainScenario> all() {
        List<NexusDomainScenario> scenarios = new ArrayList<NexusDomainScenario>();
        scenarios.add(NexusDomainScenarios.inventoryUnavailable());
        scenarios.add(NexusDomainScenarios.inventoryAvailable());
        scenarios.add(NexusDomainScenarios.securityInvestigation());
        scenarios.add(NexusDomainScenarios.securityRoutine());
        return scenarios;
    }
}
