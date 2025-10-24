package ai.braineous.app.fno.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.services.cgo.FactExtractor;

public class Flight implements FactExtractor{

    private String id;
    private String number;
    private Airport origin;
    private Airport dest;
    private String depUtc;
    private String arrUtc;
    private String capacity;
    private String equipment;

    public Flight(){

    }

    public Flight(String arrUtc, String capacity, String depUtc, Airport dest, String equipment, String id, String number, Airport origin) {
        this.arrUtc = arrUtc;
        this.capacity = capacity;
        this.depUtc = depUtc;
        this.dest = dest;
        this.equipment = equipment;
        this.id = id;
        this.number = number;
        this.origin = origin;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Airport getOrigin() {
        return origin;
    }

    public void setOrigin(Airport origin) {
        this.origin = origin;
    }

    public Airport getDest() {
        return dest;
    }

    public void setDest(Airport dest) {
        this.dest = dest;
    }

    public String getDepUtc() {
        return depUtc;
    }

    public void setDepUtc(String depUtc) {
        this.depUtc = depUtc;
    }

    public String getArrUtc() {
        return arrUtc;
    }

    public void setArrUtc(String arrUtc) {
        this.arrUtc = arrUtc;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Flight{");
        sb.append("id=").append(id);
        sb.append(", number=").append(number);
        sb.append(", origin=").append(origin);
        sb.append(", dest=").append(dest);
        sb.append(", depUtc=").append(depUtc);
        sb.append(", arrUtc=").append(arrUtc);
        sb.append(", capacity=").append(capacity);
        sb.append(", equipment=").append(equipment);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public List<Fact> extract(String prompt, JsonArray factsArray) {
        List<Fact> facts = new ArrayList<>();

        String originStr = this.origin.getIata();
        String destStr = this.dest.getIata();


        //Airport Fact
        Fact legFact = new Fact();
        legFact.setText("Airport("+ originStr + " , '"+ destStr +"')");
        legFact.setId("Flight: " + originStr + "->" + destStr);
        facts.add(legFact);
        //TODO: feats

        //TODO: other facts

        //Flight Fact

        
        //Scheduled Arrival Fact


        //Scheduled Departure Fact

        //Flight Duration Fact (derived) (arrival - departure)

        return facts;
    }

}
