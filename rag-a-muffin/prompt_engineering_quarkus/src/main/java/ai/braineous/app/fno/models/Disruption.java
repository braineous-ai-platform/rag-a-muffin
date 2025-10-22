package ai.braineous.app.fno.models;

import java.time.OffsetDateTime;

public class Disruption {

    private String type; // cancelled | delay | swap
    private String flightId;
    private String reason = "ops";
    private OffsetDateTime startUtc;
    private OffsetDateTime endUtc;

    public Disruption() {
    }

    public Disruption(OffsetDateTime endUtc, String flightId, OffsetDateTime startUtc, String type) {
        this.endUtc = endUtc;
        this.flightId = flightId;
        this.startUtc = startUtc;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public OffsetDateTime getStartUtc() {
        return startUtc;
    }

    public void setStartUtc(OffsetDateTime startUtc) {
        this.startUtc = startUtc;
    }

    public OffsetDateTime getEndUtc() {
        return endUtc;
    }

    public void setEndUtc(OffsetDateTime endUtc) {
        this.endUtc = endUtc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Disruption{");
        sb.append("type=").append(type);
        sb.append(", flightId=").append(flightId);
        sb.append(", reason=").append(reason);
        sb.append(", startUtc=").append(startUtc);
        sb.append(", endUtc=").append(endUtc);
        sb.append('}');
        return sb.toString();
    }
}
