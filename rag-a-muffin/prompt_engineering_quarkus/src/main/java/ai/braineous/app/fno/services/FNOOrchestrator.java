package ai.braineous.app.fno.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ai.braineous.rag.prompt.utils.Console;

public class FNOOrchestrator {


    public void orchestrate(JsonArray flightsArray, JsonObject factsJson){
        Console.log("flights_events", flightsArray);
        Console.log("facts_json", factsJson);

        //TODO: bridge to CGO
    }
}
