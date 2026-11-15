package ai.braineous.cgo.prompt;

import ai.braineous.cgo.prompt.scenario.NexusDomainExecutionHarness;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenarios;
import org.junit.jupiter.api.Test;

public class NexusDomainScenarioIT {

    private static final int INVOCATIONS = 20;

    private final NexusDomainExecutionHarness harness =
            new NexusDomainExecutionHarness();

    @Test
    public void test_1() {
        this.harness.run(NexusDomainScenarios.inventoryUnavailable(), INVOCATIONS);
    }

    @Test
    public void test_2() {
        this.harness.run(NexusDomainScenarios.inventoryAvailable(), INVOCATIONS);
    }

    @Test
    public void test_3() {
        this.harness.run(NexusDomainScenarios.securityInvestigation(), INVOCATIONS);
    }

    @Test
    public void test_4() {
        this.harness.run(NexusDomainScenarios.securityRoutine(), INVOCATIONS);
    }
}
