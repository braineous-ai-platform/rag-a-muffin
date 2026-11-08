package ai.braineous.cgo.nexus.harness;

import ai.braineous.motion.it.jit.Inventory;
import ai.braineous.motion.it.jit.JitFactExtractor;
import ai.braineous.motion.it.jit.JitRelationshipProvider;
import ai.braineous.motion.it.jit.Location;
import ai.braineous.motion.it.jit.Product;
import ai.braineous.motion.it.jit.Sales;
import ai.braineous.motion.it.jit.Vendor;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.models.cgo.graph.BindResult;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.models.cgo.graph.Input;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Phase0GraphHarness {

    public static class EstablishedGraph {
        private final GraphSnapshot snapshot;
        private final Fact axis;
        private final List<BindResult> bindResults;
        private final int instanceCount;

        public EstablishedGraph(
                GraphSnapshot snapshot,
                Fact axis,
                List<BindResult> bindResults,
                int instanceCount) {
            this.snapshot = snapshot;
            this.axis = axis;
            this.bindResults = bindResults;
            this.instanceCount = instanceCount;
        }

        public GraphSnapshot getSnapshot() {
            return this.snapshot;
        }

        public Fact getAxis() {
            return this.axis;
        }

        public List<BindResult> getBindResults() {
            return this.bindResults;
        }

        public int getInstanceCount() {
            return this.instanceCount;
        }
    }

    public EstablishedGraph establish(int instanceCount) {
        GraphBuilder graphBuilder = GraphBuilder.getInstance();
        JitFactExtractor extractor = new JitFactExtractor();
        JitRelationshipProvider provider = new JitRelationshipProvider();
        Gson gson = new Gson();

        Fact axis = null;
        List<BindResult> bindResults = new ArrayList<BindResult>();

        int instance = 1;
        while (instance <= instanceCount) {
            List<Fact> instanceFacts =
                    extractor.extract(this.instancePayload(gson, instance));

            int factIndex = 0;
            while (factIndex < instanceFacts.size()) {
                Fact fact = instanceFacts.get(factIndex);
                graphBuilder.addNode(fact);
                if (axis == null
                        && fact != null
                        && "Inventory:INV-001".equals(fact.getId())) {
                    axis = fact;
                }
                factIndex = factIndex + 1;
            }

            List<Relationship> instanceRelationships =
                    provider.provideRelationships(instanceFacts);
            int bindIndex = 0;
            while (bindIndex < instanceRelationships.size()) {
                Relationship relationship = instanceRelationships.get(bindIndex);
                Input input = new Input(
                        relationship.getFrom(),
                        relationship.getTo(),
                        relationship.getEdge());
                BindResult bindResult = graphBuilder.bind(input, null);
                bindResults.add(bindResult);
                Edge bindEdge = (Edge) relationship.getEdge();
                System.out.print("instance ");
                System.out.print(String.valueOf(instance));
                System.out.print("\n");
                System.out.print(bindEdge == null ? "null" : String.valueOf(bindEdge.getId()));
                System.out.print("\n");
                System.out.print(String.valueOf(bindResult.isOk()));
                System.out.print("\n");
                bindIndex = bindIndex + 1;
            }

            instance = instance + 1;
        }

        return new EstablishedGraph(
                graphBuilder.snapshot(),
                axis,
                bindResults,
                instanceCount);
    }

    private String instancePayload(Gson gson, int instance) {
        String seq = String.format("%03d", instance);
        int productNumber = 249 + instance;

        Product instanceProduct = new Product();
        instanceProduct.setProductId("PROD-SHAMPOO-" + productNumber);
        instanceProduct.setSku("SHAMPOO-" + productNumber);
        instanceProduct.setName("Shampoo " + productNumber + " mL");
        instanceProduct.setUnitOfMeasure("bottle");

        Location instanceLocation = new Location();
        instanceLocation.setLocationId("LOC-" + seq);
        if (instance == 1) {
            instanceLocation.setName("Primary Inventory Location");
        } else {
            instanceLocation.setName("Location " + seq);
        }
        instanceLocation.setLocationType("storage");

        Vendor instanceVendor = new Vendor();
        instanceVendor.setVendorId("VENDOR-" + seq);
        if (instance == 1) {
            instanceVendor.setName("Shampoo Supplier");
            instanceVendor.setLeadTimeDays(7);
        } else {
            instanceVendor.setName("Shampoo Supplier " + seq);
            instanceVendor.setLeadTimeDays(7 + instance);
        }
        instanceVendor.setProduct(instanceProduct);

        Sales instanceSales = new Sales();
        instanceSales.setSaleId("SALE-" + seq);
        if (instance == 1) {
            instanceSales.setQuantitySold(25);
            instanceSales.setSoldAt(1792584000000L);
        } else {
            instanceSales.setQuantitySold(25 + instance);
            instanceSales.setSoldAt(1792584000000L + instance);
        }
        instanceSales.setProduct(instanceProduct);

        Inventory instanceInventory = new Inventory();
        instanceInventory.setInventoryId("INV-" + seq);
        if (instance == 1) {
            instanceInventory.setQuantityOnHand(500);
        } else {
            instanceInventory.setQuantityOnHand(500 + instance);
        }
        instanceInventory.setQuantityReserved(0);
        instanceInventory.setProduct(instanceProduct);
        instanceInventory.setLocation(instanceLocation);

        JsonObject payload = new JsonObject();
        payload.add("product", gson.toJsonTree(instanceProduct));
        payload.add("inventory", gson.toJsonTree(instanceInventory));
        payload.add("sales", gson.toJsonTree(instanceSales));
        payload.add("vendor", gson.toJsonTree(instanceVendor));
        payload.add("location", gson.toJsonTree(instanceLocation));
        return payload.toString();
    }
}
