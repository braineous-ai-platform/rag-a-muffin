package ai.braineous.app.fno.models;

import java.util.List;

import com.google.gson.JsonArray;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.FactExtractor;

public class Airport {

    private String id;

    private String iata;

    private String tz;

    public Airport(){

    }

    public Airport(String iata, String id, String tz) {
        this.iata = iata;
        this.id = id;
        this.tz = tz;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public String getTz() {
        return tz;
    }

    public void setTz(String tz) {
        this.tz = tz;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Airport{");
        sb.append("id=").append(id);
        sb.append(", iata=").append(iata);
        sb.append(", tz=").append(tz);
        sb.append('}');
        return sb.toString();
    }
}
