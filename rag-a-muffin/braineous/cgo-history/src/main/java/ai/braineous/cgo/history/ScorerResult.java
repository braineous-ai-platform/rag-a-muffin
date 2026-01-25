package ai.braineous.cgo.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core, domain-agnostic result of scoring.
 *
 * - Scorer fills this.
 * - WHY()/Observability reads this.
 * - Infra (logs, Mongo, metrics) can serialize this.
 *
 * Current phase: simple status + optional score + reasons.
 * Future: you can enrich details, but keep it domain-neutral.
 */
public class ScorerResult {

    /**
     * High-level band for the score.
     * Keep this small and stable; WHY() and dashboards will key off it.
     */
    public enum Status {
        UNKNOWN,   // default, not scored
        OK,        // looks fine / low risk
        WARN,      // something smells off but not fatal
        FAIL       // hard failure / block
    }

    // --- Core fields ---

    private Status status = Status.UNKNOWN;

    /**
     * Optional numeric score (e.g. 0.0–1.0).
     * You don't have to use it in phase 1; keep null or -1 as "not set".
     */
    private Double score;

    /**
     * Short machine-friendly tag, e.g. "NO_HISTORY", "TOO_MANY_FAILURES".
     * This is what WHY()/Observability and dashboards can group on.
     */
    private String reasonCode;

    /**
     * Human-friendly one-line summary for logs / UI.
     */
    private String summary;

    /**
     * Detailed explanations / contributing factors.
     * WHY() can expand on these or add more structure later.
     */
    private final List<String> reasons = new ArrayList<>();

    public ScorerResult() {
        // default: UNKNOWN, no score
    }

    public ScorerResult(Double score) {
        this.score = score;
    }

    // --- Status / score ---

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            return;
        }
        this.status = status;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    // --- Reasoning / explanation hooks ---

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getReasons() {
        return Collections.unmodifiableList(reasons);
    }

    public void addReason(String reason) {
        if (reason != null && !reason.isBlank()) {
            this.reasons.add(reason);
        }
    }

    @Override
    public String toString() {
        return "ScorerResult{" +
                "status=" + status +
                ", score=" + score +
                ", reasonCode='" + reasonCode + '\'' +
                ", summary='" + summary + '\'' +
                ", reasons=" + reasons +
                '}';
    }

    // --- Convenience factory methods (optional sugar) ---

    public static ScorerResult unknown(String summary) {
        ScorerResult r = new ScorerResult();
        r.setStatus(Status.UNKNOWN);
        r.setSummary(summary);
        return r;
    }

    public static ScorerResult ok(String summary) {
        ScorerResult r = new ScorerResult();
        r.setStatus(Status.OK);
        r.setSummary(summary);
        return r;
    }

    public static ScorerResult warn(String reasonCode, String summary) {
        ScorerResult r = new ScorerResult();
        r.setStatus(Status.WARN);
        r.setReasonCode(reasonCode);
        r.setSummary(summary);
        return r;
    }

    public static ScorerResult fail(String reasonCode, String summary) {
        ScorerResult r = new ScorerResult();
        r.setStatus(Status.FAIL);
        r.setReasonCode(reasonCode);
        r.setSummary(summary);
        return r;
    }

    //----------------------------
    public com.google.gson.JsonObject toJson() {

        com.google.gson.JsonObject out = new com.google.gson.JsonObject();

        if (this.status != null) {
            out.addProperty("status", this.status.name());
        } else {
            out.add("status", null);
        }

        if (this.score != null) {
            out.addProperty("score", this.score);
        } else {
            out.add("score", null);
        }

        if (this.reasonCode != null) {
            out.addProperty("reasonCode", this.reasonCode);
        } else {
            out.add("reasonCode", null);
        }

        if (this.summary != null) {
            out.addProperty("summary", this.summary);
        } else {
            out.add("summary", null);
        }

        com.google.gson.JsonArray reasonsArr = new com.google.gson.JsonArray();
        for (String r : this.reasons) {
            if (r != null) {
                reasonsArr.add(r);
            }
        }
        out.add("reasons", reasonsArr);

        return out;
    }

    public static ScorerResult fromJson(com.google.gson.JsonObject json) {

        if (json == null) {
            throw new IllegalArgumentException("ScorerResult JSON cannot be null");
        }

        ScorerResult out = new ScorerResult();

        if (json.has("status") && !json.get("status").isJsonNull()) {
            String s = json.get("status").getAsString();
            try {
                out.setStatus(Status.valueOf(s));
            } catch (IllegalArgumentException e) {
                out.setStatus(Status.UNKNOWN);
            }
        }

        if (json.has("score") && !json.get("score").isJsonNull()) {
            out.setScore(json.get("score").getAsDouble());
        }

        if (json.has("reasonCode") && !json.get("reasonCode").isJsonNull()) {
            out.setReasonCode(json.get("reasonCode").getAsString());
        }

        if (json.has("summary") && !json.get("summary").isJsonNull()) {
            out.setSummary(json.get("summary").getAsString());
        }

        if (json.has("reasons")
                && !json.get("reasons").isJsonNull()
                && json.get("reasons").isJsonArray()) {

            for (int i = 0; i < json.getAsJsonArray("reasons").size(); i++) {
                if (!json.getAsJsonArray("reasons").get(i).isJsonNull()) {
                    String r = json.getAsJsonArray("reasons").get(i).getAsString();
                    if (r != null && !r.trim().isEmpty()) {
                        out.addReason(r);
                    }
                }
            }
        }

        return out;
    }
}