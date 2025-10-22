package ai.braineous.app.fno.models;

public class Constraints {

    //TODO: [configuration] per tenant (airline)
    private long minConnectMinutes = 35L; //configurabe. 
    //TODO: configuration per tenant (airline)
    private long maxConnectMinutes = 240L; // 4 hours × 60 configurable. 

    public Constraints() {
    }

    public Constraints(long minConnectMinutes, long maxConnectMinutes){
        this.maxConnectMinutes = minConnectMinutes;
        this.maxConnectMinutes = maxConnectMinutes;
    }

    public long getMinConnectMinutes() {
        return minConnectMinutes;
    }

    public void setMinConnectMinutes(long minConnectMinutes) {
        this.minConnectMinutes = minConnectMinutes;
    }

    public long getMaxConnectMinutes() {
        return maxConnectMinutes;
    }

    public void setMaxConnectMinutes(long maxConnectMinutes) {
        this.maxConnectMinutes = maxConnectMinutes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Constraints{");
        sb.append("minConnectMinutes=").append(minConnectMinutes);
        sb.append(", maxConnectMinutes=").append(maxConnectMinutes);
        sb.append('}');
        return sb.toString();
    }
}
