package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;

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
}
