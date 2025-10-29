package ai.braineous.app.fno.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.LLMBridge;
import ai.braineous.rag.prompt.services.cgo.LLMContext;
import ai.braineous.rag.prompt.services.cgo.causal.CausalLLMBridge;

public class FNOOrchestrator {

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private FNORuleProducer ruleProducer = new FNORuleProducer();

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private LLMBridge llmBridge = new CausalLLMBridge();

    public void orchestrate(JsonArray flightsJsonArray){
        try{
            LLMContext context = new LLMContext();

            Function<String, List<Fact>> factExtractor = this.getFlightFactExtractor();
            Function<List<Fact>, Set<String>> ruleGen = this.getFlightFactRuleGen();

            context.build("flights", flightsJsonArray.toString(), factExtractor, ruleGen);

            //bridge to CGO
            this.llmBridge.submit(context);

        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    
    private Function<String, List<Fact>> getFlightFactExtractor(){
        Function<String, List<Fact>> flightExtractor = (jsonArrayStr) -> {
            List<Fact> facts = new ArrayList<>();

            JsonArray flightsArray = JsonParser.parseString(jsonArrayStr).getAsJsonArray();
            for(int i=0; i<flightsArray.size(); i++){
                JsonObject o = flightsArray.get(i).getAsJsonObject();

                String id    = o.get("id").getAsString();        // "F102"
                String src   = o.get("origin").getAsString();    // "AUS"
                String dst   = o.get("dest").getAsString();      // "DFW"
                String depZ  = o.get("dep_utc").getAsString();   // "2025-10-22T11:30:00Z"
                String arrZ  = o.get("arr_utc").getAsString();   // "2025-10-22T12:40:00Z"

                // Airport facts (one per station)
                String srcAirportId = "Airport:" + src;
                String srcAirportText = "Airport(" + src + ", '" + src + "')";
                String dstAirportId = "Airport:" + dst;
                String dstAirportText = "Airport(" + dst + ", '" + dst + "')";
                facts.add(new Fact(srcAirportId, srcAirportText));
                facts.add(new Fact(dstAirportId, dstAirportText));


                // Flight fact (canonical)
                String flightText = "Flight(id:'" + id + "', " + src + ", " + dst + ", '" + depZ + "', '" + arrZ + "')";
                facts.add(new Fact("Flight:" + id, flightText));
            }

            return facts;
        };
        return flightExtractor;
    }

    private Function<List<Fact>, Set<String>> getFlightFactRuleGen(){
        Function<List<Fact>, Set<String>> ruleGen = (facts) -> {
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
        };
        return ruleGen;
    }
}
