package ai.braineous.app.fno.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ai.braineous.app.fno.models.Flight;
import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.LLMBridge;
import ai.braineous.rag.prompt.services.cgo.causal.CausalLLMBridge;
import ai.braineous.rag.prompt.utils.Console;

public class FNOOrchestrator {

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private FNORuleProducer ruleProducer = new FNORuleProducer();

    //TODO: [eventually] : make_it_quarkus_containarized. 
    //For now no dependency_injection overhead
    //@Inject
    private LLMBridge llmBridge = new CausalLLMBridge();

    public void orchestrate(JsonArray flightsArray){
        try{
            Console.log("flights_events", flightsArray);

            //get_a_list_of_flights
            List<Flight> flights = this.listOfFlights(flightsArray);
            Console.log("flights", flights);

            //get_a_list_of_facts
            List<Fact> facts = this.listOfFacts(flights);
            Console.log("facts", facts);

            //get_a_list_of_rules
            Set<String> rules = this.listOfRules(facts);
            Console.log("rules", rules);

            //TODO: bridge to CGO
            this.llmBridge.submit(facts, rules);

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<Flight> listOfFlights(JsonArray flightsArray){
        List<Flight> flights = new ArrayList<>();

        for(int i=0; i<flightsArray.size(); i++){
            JsonObject flightJson = flightsArray.get(i).getAsJsonObject();

            Flight flight = new Flight(flightJson.toString());

            flights.add(flight);
        }

        return flights;
    }

    private List<Fact> listOfFacts(List<Flight> flights){
        List<Fact> facts = new ArrayList<>();

        for(Flight flight: flights){
            List<Fact> local = flight.extract("", new JsonArray());
            facts.addAll(local);
        }

        return facts;
    }

    private Set<String> listOfRules(List<Fact> facts) throws Exception{
        Set<String> rules = this.ruleProducer.produce(facts);
        return rules;
    }
}
