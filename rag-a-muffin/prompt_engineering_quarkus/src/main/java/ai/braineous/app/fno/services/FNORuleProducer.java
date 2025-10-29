package ai.braineous.app.fno.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ai.braineous.rag.prompt.models.cgo.Fact;

public class FNORuleProducer {

    public Set<String> produce(List<Fact> facts) throws Exception{
        Set<String> out = new LinkedHashSet<>();
        Set<String> airports = new LinkedHashSet<>();
        List<String> flights = new ArrayList<>();

        for (Fact f : facts) {
            String t = f.getText();
            if (t.startsWith("Airport(")) {
                int p1 = t.indexOf('(')+1, p2 = t.indexOf(',', p1);
                String code = t.substring(p1, p2).trim();
                airports.add(code);
            } else if (t.startsWith("Flight(")) {
                flights.add(t);
            }
        }
        
        // Emit airport nodes
        for (String code : airports) {
            out.add(("{\"id\":\"R_airport_node_%s\",\"note\":\"Create airport node.\","
                + "\"when\":[\"Airport($code, $name)\"],"
                + "\"then\":[{\"emit\":\"GraphNode(%s, 'airport', $name)\"}],"
                + "\"weight\":0.8}").formatted(code, code));
        }

        // Emit flight edges (generic pattern suffices; string-only)
        if (!flights.isEmpty()) {
            out.add("{\"id\":\"R_flight_edge\",\"note\":\"Create flight edge from Flight facts.\","
                + "\"when\":[\"Flight(id:$fid, $src, $dst, $depUtc, $arrUtc)\"],"
                + "\"then\":[{\"emit\":\"GraphEdge($src, $dst, $depUtc, $arrUtc, 'fly', id:$fid)\"}],"
                + "\"weight\":1.0}");
        }
        
        return out;
    }
}
