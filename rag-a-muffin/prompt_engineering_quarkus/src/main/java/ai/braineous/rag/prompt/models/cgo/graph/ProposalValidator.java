package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Edge;
import ai.braineous.rag.prompt.models.cgo.Fact;

import java.util.Set;

public class ProposalValidator {


    public boolean validate(ProposalContext ctx){

        return true;
    }


    private boolean validate(Proposal proposal){
        Set<Fact> inserts = proposal.getInsert();
        Set<Fact> updates = proposal.getUpdate();
        Set<Fact> deletes = proposal.getDelete();
        Set<Relationship> relationships = proposal.getEdges();

        //validate_facts
        for(Fact fact:inserts){
            //TODO: finalize_later
            GraphView view = new GraphView() {
                @Override
                public Fact getFactById(String id) {
                    if ("F1".equals(id)) {
                        return fact;
                    }
                    return null;
                }
            };
            this.validateFact(fact, view);
        }
        for(Fact fact:updates){
            //TODO: finalize_later
            GraphView view = new GraphView() {
                @Override
                public Fact getFactById(String id) {
                    if ("F1".equals(id)) {
                        return fact;
                    }
                    return null;
                }
            };
            this.validateFact(fact, view);
        }
        for(Fact fact:deletes){
            //TODO: finalize_later
            GraphView view = new GraphView() {
                @Override
                public Fact getFactById(String id) {
                    if ("F1".equals(id)) {
                        return fact;
                    }
                    return null;
                }
            };
            this.validateFact(fact, view);
        }

        //validate_relationships
        /*for(Relationship relationship:relationships){
            //TODO: finalize_later
            GraphView view = new GraphView() {
                @Override
                public Fact getFactById(String id) {
                    if ("F1".equals(id)) {
                        return fact;
                    }
                    return null;
                }
            };
            this.validateFact(fact, view);
        }*/

        return true;
    }

    private boolean validateFact(Fact fact, GraphView graphView){

        return true;
    }

    private boolean validateRelationship(Relationship relationship, GraphView graphView){

        return true;
    }
}
