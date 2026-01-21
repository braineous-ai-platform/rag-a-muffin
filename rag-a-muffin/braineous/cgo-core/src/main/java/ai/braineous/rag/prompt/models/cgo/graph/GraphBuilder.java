package ai.braineous.rag.prompt.models.cgo.graph;


import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.models.cgo.graph.commit.CommitOrchestrator;
import ai.braineous.rag.prompt.models.cgo.graph.commit.CommitResult;
import ai.braineous.rag.prompt.models.cgo.graph.mutation.MutationOrchestrator;
import ai.braineous.rag.prompt.models.cgo.graph.mutation.MutationResult;
import ai.braineous.rag.prompt.models.cgo.graph.mutation.MutationResultListener;

import java.util.HashSet;
import java.util.Set;

public class GraphBuilder {
    private static GraphBuilder graphBuilder = new GraphBuilder();

    private final Validator validator = Validator.getInstance();

    private final ProposalMonitor proposalMonitor = ProposalMonitor.getInstance();

    private final GraphStore store = GraphStoreImpl.getInstance();

    private final MutationOrchestrator mutationOrchestrator = MutationOrchestrator.getInstance();

    private final CommitOrchestrator commitOrchestrator = CommitOrchestrator.getInstance();

    private GraphBuilder(){

    }

    public static GraphBuilder getInstance(){
        return GraphBuilder.graphBuilder;
    }

    public void clear(){
        ((GraphStoreImpl)store).clear();
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

        // substrate validation
        BindResult substrate = this.validateSubstrate(input);
        if (!substrate.isOk()) {
            return substrate;
        }

        // 1️⃣ base proposal ALWAYS
        Set<Proposal> proposals = new HashSet<>();
        Proposal proposal = Proposal.from(input.getFrom(), input.getTo(), input.getEdge());
        if(proposal != null) {
            proposals.add(
                proposal
            );
        }

        // 2️⃣ rulepack is an overlay
        if (rulepack != null) {
            Set<Proposal> ruleProposals = this.execute(rulepack);
            if (ruleProposals != null && !ruleProposals.isEmpty()) {
                proposals.addAll(ruleProposals);
            }
        }

        // 3️⃣ structural validation
        BindResult structure = this.validateStructure(proposals);
        if (!structure.isOk()) {
            return structure;
        }

        // 4️⃣ mutation validation
        MutationResult mr = this.validateMutation(input, rulepack, proposals);
        if (mr == null || !mr.isOk()) {
            return new BindResult(false);
        }

        // 5️⃣ commit (single choke point)
        CommitResult cr = this.commitMutation(mr);
        if (cr == null || !cr.isOk()) {
            return new BindResult(false);
        }

        return new BindResult(true);
    }



    public GraphSnapshot snapshot() {
        return store.snapshot();
    }

    //---mutation phases ----------------------------------------
    private MutationResult validateMutation(Input input, Rulepack rulepack, Set<Proposal> proposals){
        if(proposals == null || proposals.isEmpty()){
            return null;
        }

        MutationResultListener listener = this.mutationOrchestrator.
                orchestrate(
                        store.snapshot().snapshotHash(),
                        input,
                        rulepack,
                        proposals);

        return listener.result();
    }


    private CommitResult commitMutation(MutationResult mr){
        CommitResult result = this.commitOrchestrator.orchestrate(mr);
        return result;
    }

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
        if(store.snapshot().nodes().get(from.getId()) == null || store.snapshot().nodes().get(to.getId()) == null){
            result.setOk(false);
            return result;
        }

        return result;
    }

    private Set<Proposal> execute(Rulepack rulepack){
        GraphView view = store.snapshot();

        Set<Proposal> proposals = rulepack.execute(view);
        for(Proposal proposal:proposals){
            proposal.setRulepack(rulepack);
        }

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
    //------------------------------------------------------------------
}
