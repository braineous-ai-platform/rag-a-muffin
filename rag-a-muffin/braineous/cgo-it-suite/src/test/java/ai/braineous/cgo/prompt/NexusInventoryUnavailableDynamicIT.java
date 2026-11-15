package ai.braineous.cgo.prompt;

import ai.braineous.cgo.prompt.scenario.NexusDomainExecutionHarness;
import ai.braineous.cgo.prompt.scenario.NexusDomainScenarios;
import org.junit.jupiter.api.Test;

public class NexusInventoryUnavailableDynamicIT {

    @Test
    public void test_1() {
        new NexusDomainExecutionHarness().run(
                NexusDomainScenarios.inventoryUnavailable(),
                1);
    }
}
