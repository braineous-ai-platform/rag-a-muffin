package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphBuilder {
    private static GraphBuilder graphBuilder = new GraphBuilder();

    private final Validator validator = Validator.getInstance();

    private final ProposalMonitor proposalMonitor = ProposalMonitor.getInstance();

    private final GraphStore store = GraphStoreImpl.getInstance();

    private GraphBuilder(){

    }

    public static GraphBuilder getInstance(){
        return GraphBuilder.graphBuilder;
    }

    public void clear(){
        store.clear();
    }

    /**
     * Directly add/merge an atomic Fact as a node.
     * Can be used independent of bind() if needed.
     */
    public void addNode(Fact fact) {
        if (fact == null) {
            return;
        }

        //TODO: activate once mutation queue and sync issues are establised
        /*boolean isValid = Validator.getInstance().validateInsert(fact);
        if(!isValid){
            return;
        }*/

        store.upsertNode(fact);
    }

    /**
     * Validate and apply a single (from, to, edgeFact) triple.
     * On failure, graph state is unchanged.
     */
    public BindResult bind(Input input, Rulepack rulepack) {
        if (input == null) {
            return new BindResult(false);
        }

        //substrate validation
        BindResult result = this.validateSubstrate(input);
        if (!result.isOk()) {
            return result;
        }

        Fact from = input.getFrom();   // atomic
        Fact to   = input.getTo();     // atomic
        Fact edgeFact = input.getEdge(); // relational-as-Fact

        if(rulepack != null) {
            //execution_phase
            Set<Proposal> proposals = this.execute(rulepack);

            //proposal_phase
            BindResult proposalResult = this.validateStructure(proposals);
            if (!proposalResult.isOk()) {
                return proposalResult;
            }
        }

        //mutate
        this.mutate(from, to, edgeFact);

        return result;
    }

    public GraphSnapshot snapshot() {
        return store.snapshot();
    }

    //---mutation phases ----------------------------------------
    private BindResult validateSubstrate(Input input){
        if (input == null) {
            return new BindResult(false);
        }

        Fact from = input.getFrom();   // atomic
        Fact to   = input.getTo();     // atomic
        Fact edgeFact = input.getEdge(); // relational-as-Fact

        if(edgeFact == null){
            return new BindResult(false);
        }

        //make the edge relational
        edgeFact.setMode("relational");

        BindResult result = this.validator.bind(input);
        if (!result.isOk()) {
            return result;
        }

        //make sure from and to exist
        if(store.nodes().get(from.getId()) == null || store.nodes().get(to.getId()) == null){
            result.setOk(false);
            return result;
        }

        return result;
    }

    private Set<Proposal> execute(Rulepack rulepack){
        GraphView view = store.snapshot();

        Set<Proposal> proposals = rulepack.execute(view);

        return proposals;
    }

    private BindResult validateStructure(Set<Proposal> proposals){
        BindResult bindResult = new BindResult(true);
        if(proposals == null || proposals.isEmpty()){
            return bindResult;
        }

        ProposalContext ctx = new ProposalContext();
        GraphSnapshot snapshot = store.snapshot();
        ctx.setProposals(proposals);
        ctx.setSnapshot(snapshot);

        //use the proposal_monitor to validate
        ctx = this.proposalMonitor.receive(ctx);
        if(ctx == null){
            bindResult.setOk(false);
            return bindResult;
        }

        bindResult.setOk(ctx.isValidationSuccess());

        return bindResult;
    }

    private void mutate(Fact from, Fact to, Fact edgeFact){
        // upsert edge
        store.mutate(from, to, edgeFact);
    }
    //------------------------------------------------------------------
}
