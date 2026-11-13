package ai.braineous.cgo.nexus.template;

import org.junit.jupiter.api.Test;

public class NexusTemplateSecurityInvestigateObservationTest {

    private final NexusTemplateObservationSupport support =
            new NexusTemplateObservationSupport();

    @Test
    public void test_1() {
        String reference = this.support.loadResource(
                "security-investigation.nexus.txt");
        this.support.observeBlocked(
                "No CGO facts, relationships, or arithmetic expression exist "
                        + "in Arc security-account-investigate.json. "
                        + "Production TemplateGenerator emits only "
                        + "\"The result is {n}.\" and cannot emit multi-field "
                        + "security context. NexusOrchestrator was not invoked "
                        + "with fabricated facts.",
                reference);
    }
}
