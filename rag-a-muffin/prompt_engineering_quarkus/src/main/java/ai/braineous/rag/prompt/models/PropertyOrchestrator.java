package ai.braineous.rag.prompt.models;

import java.util.ArrayList;
import java.util.List;

public class PropertyOrchestrator {
    private List<Property> properties;

    public PropertyOrchestrator(){
        this.properties = new ArrayList<>();
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public void addProperty(Property property){
        this.properties.add(property);
    }

    public boolean removeProperty(Property property){
        return this.properties.remove(property);
    }
}
