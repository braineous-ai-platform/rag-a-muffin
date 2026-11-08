package ai.braineous.motion.it.jit;

public class Sales {

    private String saleId;

    private int quantitySold;

    private long soldAt;

    private Product product;

    public Sales() {
    }

    public String getSaleId() {
        return this.saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public int getQuantitySold() {
        return this.quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public long getSoldAt() {
        return this.soldAt;
    }

    public void setSoldAt(long soldAt) {
        this.soldAt = soldAt;
    }

    public Product getProduct() {
        return this.product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
