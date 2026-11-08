package ai.braineous.motion.it.jit;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class JitFactExtractor implements FactExtractor {

    private final Gson gson = new Gson();

    @Override
    public List<Fact> extract(String json) {
        List<Fact> facts = new ArrayList<Fact>();

        if (json == null || json.trim().isEmpty()) {
            return facts;
        }

        JsonObject root;

        try {
            root = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            return facts;
        }

        if (root.has("product")) {
            this.addProductFact(facts, root.get("product"));
        }

        if (root.has("inventory")) {
            this.addInventoryFact(facts, root.get("inventory"));
        }

        if (root.has("sales")) {
            this.addSalesFact(facts, root.get("sales"));
        }

        if (root.has("vendor")) {
            this.addVendorFact(facts, root.get("vendor"));
        }

        if (root.has("location")) {
            this.addLocationFact(facts, root.get("location"));
        }

        return facts;
    }

    private void addProductFact(List<Fact> facts, JsonElement element) {
        Product product = this.read(element, Product.class);

        if (product == null) {
            return;
        }

        String factId = this.factId("Product", product.getProductId());

        if (factId == null) {
            return;
        }

        JsonObject factJson = this.baseFact(factId, "Product");
        this.addText(factJson, "productId", product.getProductId());
        this.addText(factJson, "sku", product.getSku());
        this.addText(factJson, "name", product.getName());
        this.addText(factJson, "unitOfMeasure", product.getUnitOfMeasure());

        facts.add(this.atomicFact(factId, factJson));
    }

    private void addInventoryFact(List<Fact> facts, JsonElement element) {
        Inventory inventory = this.read(element, Inventory.class);

        if (inventory == null) {
            return;
        }

        String factId = this.factId("Inventory", inventory.getInventoryId());

        if (factId == null) {
            return;
        }

        JsonObject factJson = this.baseFact(factId, "Inventory");
        this.addText(factJson, "inventoryId", inventory.getInventoryId());
        factJson.addProperty("quantityOnHand", inventory.getQuantityOnHand());
        factJson.addProperty("quantityReserved", inventory.getQuantityReserved());
        this.addText(factJson, "product", this.productFactId(inventory.getProduct()));
        this.addText(factJson, "location", this.locationFactId(inventory.getLocation()));

        facts.add(this.atomicFact(factId, factJson));
    }

    private void addSalesFact(List<Fact> facts, JsonElement element) {
        Sales sales = this.read(element, Sales.class);

        if (sales == null) {
            return;
        }

        String factId = this.factId("Sales", sales.getSaleId());

        if (factId == null) {
            return;
        }

        JsonObject factJson = this.baseFact(factId, "Sales");
        this.addText(factJson, "saleId", sales.getSaleId());
        factJson.addProperty("quantitySold", sales.getQuantitySold());
        factJson.addProperty("soldAt", sales.getSoldAt());
        this.addText(factJson, "product", this.productFactId(sales.getProduct()));

        facts.add(this.atomicFact(factId, factJson));
    }

    private void addVendorFact(List<Fact> facts, JsonElement element) {
        Vendor vendor = this.read(element, Vendor.class);

        if (vendor == null) {
            return;
        }

        String factId = this.factId("Vendor", vendor.getVendorId());

        if (factId == null) {
            return;
        }

        JsonObject factJson = this.baseFact(factId, "Vendor");
        this.addText(factJson, "vendorId", vendor.getVendorId());
        this.addText(factJson, "name", vendor.getName());
        factJson.addProperty("leadTimeDays", vendor.getLeadTimeDays());
        this.addText(factJson, "product", this.productFactId(vendor.getProduct()));

        facts.add(this.atomicFact(factId, factJson));
    }

    private void addLocationFact(List<Fact> facts, JsonElement element) {
        Location location = this.read(element, Location.class);

        if (location == null) {
            return;
        }

        String factId = this.factId("Location", location.getLocationId());

        if (factId == null) {
            return;
        }

        JsonObject factJson = this.baseFact(factId, "Location");
        this.addText(factJson, "locationId", location.getLocationId());
        this.addText(factJson, "name", location.getName());
        this.addText(factJson, "locationType", location.getLocationType());

        facts.add(this.atomicFact(factId, factJson));
    }

    private <T> T read(JsonElement element, Class<T> type) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return null;
        }

        try {
            return this.gson.fromJson(element, type);
        } catch (Exception e) {
            return null;
        }
    }

    private JsonObject baseFact(String factId, String kind) {
        JsonObject factJson = new JsonObject();
        factJson.addProperty("id", factId);
        factJson.addProperty("kind", kind);
        factJson.addProperty("mode", "atomic");
        return factJson;
    }

    private Fact atomicFact(String factId, JsonObject factJson) {
        Fact fact = new Fact(factId, factJson.toString());
        fact.setMode("atomic");
        return fact;
    }

    private void addText(JsonObject factJson, String name, String value) {
        if (value == null) {
            factJson.add(name, null);
        } else {
            factJson.addProperty(name, value);
        }
    }

    private String productFactId(Product product) {
        if (product == null) {
            return null;
        }

        return this.factId("Product", product.getProductId());
    }

    private String locationFactId(Location location) {
        if (location == null) {
            return null;
        }

        return this.factId("Location", location.getLocationId());
    }

    private String factId(String kind, String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        return kind + ":" + id;
    }
}
