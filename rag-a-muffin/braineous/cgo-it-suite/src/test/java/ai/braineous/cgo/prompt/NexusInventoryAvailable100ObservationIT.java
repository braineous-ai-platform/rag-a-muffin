package ai.braineous.cgo.prompt;

import ai.braineous.cgo.prompt.scenario.NexusDomainExecutionHarness;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenarios;
import org.junit.jupiter.api.Test;

public class NexusInventoryAvailable100ObservationIT {

    @Test
    public void test_1() {
        new NexusDomainExecutionHarness().run(
                NexusDomainScenarios.inventoryAvailable(),
                1);
    }
}
