package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;

public class Validator {

    public BindResult bind(Input input){
        if(input == null){
            return new BindResult(false);
        }

        BindResult bindResult = new BindResult(true);

        Fact from = input.getFrom();
        Fact to = input.getTo();
        Fact edge = input.getEdge();


        //make sure none of the Facts are null
        if(from == null || to == null || edge == null
        ){
            return new BindResult(false);
        }

        String fromMode = from.getMode();
        String toMode = to.getMode();
        String edgeMode = edge.getMode();
        if(fromMode == null || toMode == null || edgeMode == null){
            return new BindResult(false);
        }

        //no self edges allowed
        if(from.equals(to)){
            return new BindResult(false);
        }

        //make sure from and to are atomic facts
        if(!fromMode.equals("atomic") || !toMode.equals("atomic"))
        {
            return new BindResult(false);
        }

        //make sure edge is a relational fact
        if(!edgeMode.equals("relational")){
            return new BindResult(false);
        }

        return bindResult;
    }

    public boolean validateInsert(Fact fact){
        if(fact == null){
            return false;
        }

        String factId = fact.getId();
        if(factId == null || factId.trim().length()==0){
            return false;
        }

        if(!fact.getMode().equals("atomic")){
            return false;
        }

        return true;
    }

    public boolean validateDelete(Fact fact){
        if(fact == null){
            return false;
        }

        String factId = fact.getId();
        if(factId == null || factId.trim().length()==0){
            return false;
        }

        if(!fact.getMode().equals("atomic")){
            return false;
        }

        return true;
    }

    public boolean validateUpdate(GraphSnapshot snapshot, Fact fact){
        if(fact == null || snapshot == null){
            return false;
        }

        String factId = fact.getId();
        if(factId == null || factId.trim().length()==0){
            return false;
        }

        if(!fact.getMode().equals("atomic")){
            return false;
        }

        if(!snapshot.doesFactExist(fact)){
            return false;
        }

        return true;
    }

    public boolean validateRelationship(GraphSnapshot snapshot, Relationship relationship){
        if(snapshot == null || relationship == null){
            return false;
        }

        Fact from = relationship.getFrom();
        Fact to = relationship.getTo();
        Fact edge = relationship.getEdge();


        //make sure none of the Facts are null
        if(from == null || to == null || edge == null
        ){
            return false;
        }

        String fromMode = from.getMode();
        String toMode = to.getMode();
        String edgeMode = edge.getMode();
        if(fromMode == null || toMode == null || edgeMode == null){
            return false;
        }

        //no self edges allowed
        if(from.equals(to)){
            return false;
        }

        //make sure from and to are atomic facts
        if(!fromMode.equals("atomic") || !toMode.equals("atomic"))
        {
            return false;
        }

        //make sure edge is a relational fact
        if(!edgeMode.equals("relational")){
            return false;
        }

        //check if both nodes exists
        boolean toExists = snapshot.doesFactExist(to);
        boolean fromExists = snapshot.doesFactExist(from);

        if(!toExists || !fromExists)
        {
            return false;
        }

        return true;
    }
}
