package ai.braineous.app.fno.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.FactExtractor;

public class Flight implements FactExtractor{

    private String json;

    public Flight(){

    }

    public Flight(String json) {
        this.json = json;
    }


    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return "Flight [json=" + json + "]";
    }


    @Override
    public List<Fact> extract(String prompt, JsonArray factsArray) {
        List<Fact> facts = new ArrayList<>();

        JsonObject o = JsonParser.parseString(this.json).getAsJsonObject();
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

        return facts;
    }

}
