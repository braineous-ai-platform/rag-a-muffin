package ai.braineous.cgo.observer;

import java.util.Objects;

public final class WhySnapshot {

    private final int totalEvents;
    private final Double lastScore;
    private final Double averageScore;

    // later we can add: perQueryKind stats, bands, etc.

    public WhySnapshot(int totalEvents, Double lastScore, Double averageScore) {
        this.totalEvents = totalEvents;
        this.lastScore = lastScore;
        this.averageScore = averageScore;
    }

    public static WhySnapshot empty() {
        return new WhySnapshot(0, null, null);
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public Double getLastScore() {
        return lastScore;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    @Override
    public String toString() {
        return "WhySnapshot{" +
                "totalEvents=" + totalEvents +
                ", lastScore=" + lastScore +
                ", averageScore=" + averageScore +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhySnapshot)) return false;
        WhySnapshot that = (WhySnapshot) o;
        return totalEvents == that.totalEvents &&
                Objects.equals(lastScore, that.lastScore) &&
                Objects.equals(averageScore, that.averageScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalEvents, lastScore, averageScore);
    }
}
