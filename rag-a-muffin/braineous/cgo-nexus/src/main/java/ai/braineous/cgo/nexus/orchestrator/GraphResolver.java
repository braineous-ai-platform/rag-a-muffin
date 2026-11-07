package ai.braineous.cgo.nexus.orchestrator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;

public class GraphResolver {

    public ResolvedGraph resolve(Fact axis, List<String> relatedFacts) {
        GraphSnapshot snapshot =
                GraphBuilder.getInstance().snapshot();

        Map<String, Fact> filteredNodes = new HashMap<String, Fact>();
        for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            if (fact == null || fact.getId() == null) {
                continue;
            }

            boolean retain = fact.getId().equals(axis.getId());
            if (!retain) {
                for (String related : relatedFacts) {
                    if (related != null && fact.getId().startsWith(related + ":")) {
                        retain = true;
                        break;
                    }
                }
            }

            if (retain) {
                filteredNodes.put(entry.getKey(), fact);
            }
        }

        Map<String, Edge> filteredEdges = new HashMap<String, Edge>();
        for (Map.Entry<String, Edge> entry : snapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            if (edge == null) {
                continue;
            }

            String fromFactId = edge.getFromFactId();
            String toFactId = edge.getToFactId();
            if (fromFactId != null
                    && toFactId != null
                    && filteredNodes.containsKey(fromFactId)
                    && filteredNodes.containsKey(toFactId)) {
                filteredEdges.put(entry.getKey(), edge);
            }
        }

        GraphSnapshot focusedSnapshot =
                new GraphSnapshot(filteredNodes, filteredEdges);

        return new ResolvedGraph(axis, focusedSnapshot);
    }
}
