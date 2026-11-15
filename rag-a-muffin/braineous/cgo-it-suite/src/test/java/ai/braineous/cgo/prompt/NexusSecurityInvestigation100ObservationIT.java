package ai.braineous.cgo.prompt;

import ai.braineous.cgo.prompt.scenario.NexusDomainExecutionHarness;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenarios;
import org.junit.jupiter.api.Test;

public class NexusSecurityInvestigation100ObservationIT {

    @Test
    public void test_1() {
        new NexusDomainExecutionHarness().run(
                NexusDomainScenarios.securityInvestigation(),
                100);
    }
}
