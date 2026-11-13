package ai.braineous.cgo.nexus.template;

import ai.braineous.cgo.nexus.orchestrator.NexusOrchestrator;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NexusTemplateSecurityInvestigationObservationTest {

    private final NexusTemplateObservationSupport support =
            new NexusTemplateObservationSupport();

    private Fact axis;

    @BeforeEach
    public void hydrateCgoState() {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();

        Fact account = this.support.fact(
                "Account:emp-042",
                "{\"id\":\"Account:emp-042\",\"kind\":\"Account\","
                        + "\"mode\":\"atomic\",\"account\":\"emp-042\"}");
        Fact location = this.support.fact(
                "Location:germany",
                "{\"id\":\"Location:germany\",\"kind\":\"Location\","
                        + "\"mode\":\"atomic\",\"location\":\"germany\"}");
        Fact device = this.support.fact(
                "Device:macbook-pro",
                "{\"id\":\"Device:macbook-pro\",\"kind\":\"Device\","
                        + "\"mode\":\"atomic\",\"device\":\"macbook pro\"}");
        Fact failedLogin = this.support.fact(
                "FailedLogin:FL-001",
                "{\"id\":\"FailedLogin:FL-001\",\"kind\":\"FailedLogin\","
                        + "\"mode\":\"atomic\",\"failedLoginAttempts\":3,"
                        + "\"account\":\"Account:emp-042\"}");
        Fact successfulLogin = this.support.fact(
                "SuccessfulLogin:SL-001",
                "{\"id\":\"SuccessfulLogin:SL-001\",\"kind\":\"SuccessfulLogin\","
                        + "\"mode\":\"atomic\",\"successfulLoginAttempts\":1,"
                        + "\"account\":\"Account:emp-042\"}");

        graphBuilder.addNode(account);
        graphBuilder.addNode(location);
        graphBuilder.addNode(device);
        graphBuilder.addNode(failedLogin);
        graphBuilder.addNode(successfulLogin);

        BindResult accountLocation = graphBuilder.bind(
                this.support.edgeInput(account, location),
                null);
        BindResult accountDevice = graphBuilder.bind(
                this.support.edgeInput(account, device),
                null);
        BindResult failedAccount = graphBuilder.bind(
                this.support.edgeInput(failedLogin, account),
                null);
        BindResult successfulAccount = graphBuilder.bind(
                this.support.edgeInput(successfulLogin, account),
                null);
        assertTrue(accountLocation.isOk());
        assertTrue(accountDevice.isOk());
        assertTrue(failedAccount.isOk());
        assertTrue(successfulAccount.isOk());

        this.axis = account;
    }

    @Test
    public void test_1() {
        assertNotNull(this.axis);

        String generated = new NexusOrchestrator().execute(
                this.axis,
                List.of("Location", "Device", "FailedLogin", "SuccessfulLogin"),
                null,
                this.support.loadResource("security-investigation.definition.txt"));
        String reference = this.support.loadResource(
                "security-investigation.nexus.txt");
        this.support.observeTitleAndContext(generated, reference);
    }
}
