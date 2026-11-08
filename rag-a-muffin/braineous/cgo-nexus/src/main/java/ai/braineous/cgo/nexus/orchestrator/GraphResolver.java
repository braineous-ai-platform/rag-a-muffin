package ai.braineous.cgo.nexus.orchestrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        Map<String, Edge> filteredEdges = new HashMap<String, Edge>();

        if (axis == null || axis.getId() == null) {
            return new ResolvedGraph(axis, new GraphSnapshot(filteredNodes, filteredEdges));
        }

        String axisId = axis.getId();
        Map<String, List<String>> adjacency = new HashMap<String, List<String>>();
        for (Map.Entry<String, Edge> entry : snapshot.edges().entrySet()) {
            Edge edge = entry.getValue();
            if (edge == null) {
                continue;
            }

            String fromFactId = edge.getFromFactId();
            String toFactId = edge.getToFactId();
            if (fromFactId == null || toFactId == null) {
                continue;
            }

            this.addAdjacency(adjacency, fromFactId, toFactId);
            this.addAdjacency(adjacency, toFactId, fromFactId);
        }

        Map<String, String> parent = new HashMap<String, String>();
        Set<String> reachable = new HashSet<String>();
        LinkedList<String> queue = new LinkedList<String>();
        reachable.add(axisId);
        queue.add(axisId);

        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            List<String> neighbors = adjacency.get(current);
            if (neighbors == null) {
                continue;
            }

            int neighborIndex = 0;
            while (neighborIndex < neighbors.size()) {
                String neighbor = neighbors.get(neighborIndex);
                if (reachable.add(neighbor)) {
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
                neighborIndex = neighborIndex + 1;
            }
        }

        Set<String> retainIds = new HashSet<String>();
        retainIds.add(axisId);

        if (relatedFacts != null) {
            for (String nodeId : reachable) {
                boolean requested = false;
                int relatedIndex = 0;
                while (relatedIndex < relatedFacts.size()) {
                    String related = relatedFacts.get(relatedIndex);
                    if (related != null && nodeId.startsWith(related + ":")) {
                        requested = true;
                        break;
                    }
                    relatedIndex = relatedIndex + 1;
                }

                if (!requested) {
                    continue;
                }

                String cursor = nodeId;
                while (cursor != null && retainIds.add(cursor)) {
                    cursor = parent.get(cursor);
                }
            }
        }

        for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
            Fact fact = entry.getValue();
            if (fact == null || fact.getId() == null) {
                continue;
            }

            if (retainIds.contains(fact.getId()) || retainIds.contains(entry.getKey())) {
                filteredNodes.put(entry.getKey(), fact);
            }
        }

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

    private void addAdjacency(
            Map<String, List<String>> adjacency,
            String fromId,
            String toId) {
        List<String> neighbors = adjacency.get(fromId);
        if (neighbors == null) {
            neighbors = new ArrayList<String>();
            adjacency.put(fromId, neighbors);
        }
        neighbors.add(toId);
    }
}
