package ai.braineous.app.fno.models;

import java.util.List;
import java.util.Map;

public class DomainState {
    private String cycleId;
    private Map<String,Flight> flights;
    private List<Disruption> disruptions;
    private Constraints constraints;

    //TODO wire in the Guava Graph data structure or a proxy to say Neo4J
    //App-Framework to platform decision wuth an interface. This can wait!
    //graph: Optional[nx.DiGraph] = None # base flight graph (non-time-expanded)

    public DomainState() {
    }

    public DomainState(Constraints constraints, String cycleId, List<Disruption> disruptions, Map<String, Flight> flights) {
        this.constraints = constraints;
        this.cycleId = cycleId;
        this.disruptions = disruptions;
        this.flights = flights;
    }

    public String getCycleId() {
        return cycleId;
    }

    public void setCycleId(String cycleId) {
        this.cycleId = cycleId;
    }

    public Map<String, Flight> getFlights() {
        return flights;
    }

    public void setFlights(Map<String, Flight> flights) {
        this.flights = flights;
    }

    public List<Disruption> getDisruptions() {
        return disruptions;
    }

    public void setDisruptions(List<Disruption> disruptions) {
        this.disruptions = disruptions;
    }

    public Constraints getConstraints() {
        return constraints;
    }

    public void setConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DomainState{");
        sb.append("cycleId=").append(cycleId);
        sb.append(", flights=").append(flights);
        sb.append(", disruptions=").append(disruptions);
        sb.append(", constraints=").append(constraints);
        sb.append('}');
        return sb.toString();
    }
}
