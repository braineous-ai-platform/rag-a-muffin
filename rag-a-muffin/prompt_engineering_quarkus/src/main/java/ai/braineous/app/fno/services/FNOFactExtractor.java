package ai.braineous.app.fno.services;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.FactExtractor;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * FNOFactExtractor
 * Input: JSON with either { "flights": [ ... ] } or a bare JSON array [ ... ].
 * Each flight:
 *   { "id":"F102", "origin":"AUS", "dest":"DFW",
 *     "dep_utc":"2025-10-22T11:30:00Z", "arr_utc":"2025-10-22T12:40:00Z" }
 *
 * Emits:
 *   Airport facts:  id="Airport:AUS", kind="Airport", features:{code:"AUS"}
 *   Flight fact:    id="Flight:F102", kind="Flight",
 *                   features:{from:"Airport:AUS", to:"Airport:DFW",
 *                             depTime:"...", arrTime:"...", carrier?}
 */
public class FNOFactExtractor implements FactExtractor {

    /**
     * Applies this function to the given argument.
     *
     * @param jsonArrayStr the function argument
     * @return the function result
     */
    @Override
    public List<Fact> extract(String jsonArrayStr) {
        List<Fact> facts = new ArrayList<>();

        JsonArray flightsArray = JsonParser.parseString(jsonArrayStr).getAsJsonArray();
        for (int i = 0; i < flightsArray.size(); i++) {
            JsonObject o = flightsArray.get(i).getAsJsonObject();

            String id = o.get("id").getAsString(); // "F102"
            String src = o.get("origin").getAsString(); // "AUS"
            String dst = o.get("dest").getAsString(); // "DFW"
            String depZ = o.get("dep_utc").getAsString(); // "2025-10-22T11:30:00Z"
            String arrZ = o.get("arr_utc").getAsString(); // "2025-10-22T12:40:00Z"

            // Airport facts (one per station)
            String srcAirportId = "Airport:" + src;
            String srcAirportText = "Airport(" + src + ", '" + src + "')";
            JsonObject srcAirportJson = new JsonObject();
            srcAirportJson.addProperty("id", srcAirportId);
            srcAirportJson.addProperty("kind", "Airport");
            srcAirportJson.addProperty("mode", "atomic");
            // TODO: feats and meta objects
            Console.log("src_airport", srcAirportJson);

            String dstAirportId = "Airport:" + dst;
            String dstAirportText = "Airport(" + dst + ", '" + dst + "')";
            JsonObject dstAirportJson = new JsonObject();
            dstAirportJson.addProperty("id", dstAirportId);
            dstAirportJson.addProperty("kind", "Airport");
            dstAirportJson.addProperty("mode", "atomic");
            // TODO: feats and meta objects
            Console.log("dst_airport", srcAirportJson);

            facts.add(new Fact(srcAirportId, srcAirportJson.toString()));
            facts.add(new Fact(dstAirportId, dstAirportJson.toString()));

            // Flight fact (canonical)
            String flightText = "Flight(id:'" + id + "', " + src + ", " + dst + ", '" + depZ + "', '" + arrZ + "')";
            JsonObject flightJson = new JsonObject();
            flightJson.addProperty("id", id);
            flightJson.addProperty("kind", "Flight");
            flightJson.addProperty("mode", "relational");
            flightJson.addProperty("from", src);
            flightJson.addProperty("to", dst);
            // TODO: feats and meta objects
            Console.log("flight", flightJson);
            facts.add(new Fact("Flight:" + id, flightJson.toString()));
        }

        return facts;
    }
}
