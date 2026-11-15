package ai.braineous.cgo.prompt.scenario;

import java.util.ArrayList;
import java.util.List;

public final class NexusDomainScenarios {

    private static final String INVENTORY_EXPRESSION =
            "(Inventory.quantityOnHand - Inventory.quantityReserved) / Sales.quantitySold";
    private static final String INVENTORY_DEFINITION = "inventory availability.";
    private static final String INVENTORY_RULE =
            "rule\n"
                    + "if the result is greater than or equal to 1,\n"
                    + "the inventory is available.\n"
                    + "otherwise, the inventory is unavailable.";
    private static final String INVENTORY_DECISION =
            "decision\n"
                    + "state the inventory status as either available or unavailable.";
    private static final String SECURITY_DEFINITION = "security investigation";
    private static final String SECURITY_RULE =
            "rule\n"
                    + "when multiple failed login attempts are followed by\n"
                    + "a successful login attempt,\n"
                    + "the activity warrants a security investigation.\n"
                    + "otherwise, routine monitoring is sufficient.";
    private static final String SECURITY_DECISION =
            "decision\n"
                    + "based on the context and the rule,\n"
                    + "does this account activity warrant a security investigation\n"
                    + "or routine monitoring?";

    private NexusDomainScenarios() {
    }

    public static NexusDomainScenario inventoryUnavailable() {
        return new NexusDomainScenario(
                "inventory-unavailable",
                "Inventory:INV-001",
                List.of("Sales"),
                INVENTORY_EXPRESSION,
                INVENTORY_DEFINITION,
                INVENTORY_RULE,
                INVENTORY_DECISION,
                "unavailable",
                "inventory availability.\n\ncontext\nThe result is 0.",
                inventoryFacts(0, 0, 25),
                inventoryEdges());
    }

    public static NexusDomainScenario inventoryAvailable() {
        return new NexusDomainScenario(
                "inventory-available",
                "Inventory:INV-001",
                List.of("Sales"),
                INVENTORY_EXPRESSION,
                INVENTORY_DEFINITION,
                INVENTORY_RULE,
                INVENTORY_DECISION,
                "available",
                "inventory availability.\n\ncontext\nThe result is 1.",
                inventoryFacts(26, 1, 25),
                inventoryEdges());
    }

    public static NexusDomainScenario securityInvestigation() {
        return new NexusDomainScenario(
                "security-investigation",
                "Account:emp-042",
                List.of("Location", "Device", "FailedLogin", "SuccessfulLogin"),
                null,
                SECURITY_DEFINITION,
                SECURITY_RULE,
                SECURITY_DECISION,
                "investigation",
                "failedLoginAttempts: 3",
                securityFacts(3, 1),
                securityEdges());
    }

    public static NexusDomainScenario securityRoutine() {
        return new NexusDomainScenario(
                "security-routine",
                "Account:emp-042",
                List.of("Location", "Device", "FailedLogin", "SuccessfulLogin"),
                null,
                SECURITY_DEFINITION,
                SECURITY_RULE,
                SECURITY_DECISION,
                "routine",
                "failedLoginAttempts: 0",
                securityFacts(0, 0),
                securityEdges());
    }

    private static List<NexusDomainFactSpec> inventoryFacts(
            int quantityOnHand,
            int quantityReserved,
            int quantitySold) {
        List<NexusDomainFactSpec> facts = new ArrayList<NexusDomainFactSpec>();
        facts.add(new NexusDomainFactSpec(
                "Product:PROD-SHAMPOO-250",
                "{\"id\":\"Product:PROD-SHAMPOO-250\",\"kind\":\"Product\","
                        + "\"mode\":\"atomic\",\"productId\":\"PROD-SHAMPOO-250\","
                        + "\"sku\":\"SHAMPOO-250\",\"name\":\"Shampoo 250 mL\","
                        + "\"unitOfMeasure\":\"bottle\"}"));
        facts.add(new NexusDomainFactSpec(
                "Inventory:INV-001",
                "{\"id\":\"Inventory:INV-001\",\"kind\":\"Inventory\","
                        + "\"mode\":\"atomic\",\"inventoryId\":\"INV-001\","
                        + "\"quantityOnHand\":" + quantityOnHand
                        + ",\"quantityReserved\":" + quantityReserved
                        + ",\"product\":\"Product:PROD-SHAMPOO-250\"}"));
        facts.add(new NexusDomainFactSpec(
                "Sales:SALE-001",
                "{\"id\":\"Sales:SALE-001\",\"kind\":\"Sales\","
                        + "\"mode\":\"atomic\",\"saleId\":\"SALE-001\","
                        + "\"quantitySold\":" + quantitySold
                        + ",\"product\":\"Product:PROD-SHAMPOO-250\"}"));
        return facts;
    }

    private static List<NexusDomainEdgeSpec> inventoryEdges() {
        List<NexusDomainEdgeSpec> edges = new ArrayList<NexusDomainEdgeSpec>();
        edges.add(new NexusDomainEdgeSpec("Inventory:INV-001", "Product:PROD-SHAMPOO-250"));
        edges.add(new NexusDomainEdgeSpec("Sales:SALE-001", "Product:PROD-SHAMPOO-250"));
        return edges;
    }

    private static List<NexusDomainFactSpec> securityFacts(
            int failedLoginAttempts,
            int successfulLoginAttempts) {
        List<NexusDomainFactSpec> facts = new ArrayList<NexusDomainFactSpec>();
        facts.add(new NexusDomainFactSpec(
                "Account:emp-042",
                "{\"id\":\"Account:emp-042\",\"kind\":\"Account\","
                        + "\"mode\":\"atomic\",\"account\":\"emp-042\"}"));
        facts.add(new NexusDomainFactSpec(
                "Location:germany",
                "{\"id\":\"Location:germany\",\"kind\":\"Location\","
                        + "\"mode\":\"atomic\",\"location\":\"germany\"}"));
        facts.add(new NexusDomainFactSpec(
                "Device:macbook-pro",
                "{\"id\":\"Device:macbook-pro\",\"kind\":\"Device\","
                        + "\"mode\":\"atomic\",\"device\":\"macbook pro\"}"));
        facts.add(new NexusDomainFactSpec(
                "FailedLogin:FL-001",
                "{\"id\":\"FailedLogin:FL-001\",\"kind\":\"FailedLogin\","
                        + "\"mode\":\"atomic\",\"failedLoginAttempts\":" + failedLoginAttempts
                        + ",\"account\":\"Account:emp-042\"}"));
        facts.add(new NexusDomainFactSpec(
                "SuccessfulLogin:SL-001",
                "{\"id\":\"SuccessfulLogin:SL-001\",\"kind\":\"SuccessfulLogin\","
                        + "\"mode\":\"atomic\",\"successfulLoginAttempts\":" + successfulLoginAttempts
                        + ",\"account\":\"Account:emp-042\"}"));
        return facts;
    }

    private static List<NexusDomainEdgeSpec> securityEdges() {
        List<NexusDomainEdgeSpec> edges = new ArrayList<NexusDomainEdgeSpec>();
        edges.add(new NexusDomainEdgeSpec("Account:emp-042", "Location:germany"));
        edges.add(new NexusDomainEdgeSpec("Account:emp-042", "Device:macbook-pro"));
        edges.add(new NexusDomainEdgeSpec("FailedLogin:FL-001", "Account:emp-042"));
        edges.add(new NexusDomainEdgeSpec("SuccessfulLogin:SL-001", "Account:emp-042"));
        return edges;
    }
}
