package ai.braineous.rag.prompt.models.cgo.graph;

import java.util.HashSet;
import java.util.Set;

public class ProposalMonitor {
    private static ProposalMonitor proposalMonitor = new ProposalMonitor();

    private ProposalMonitor(){

    }

    public static ProposalMonitor getInstance(){
        return ProposalMonitor.proposalMonitor;
    }

    public ProposalContext receive(ProposalContext ctx){
        if(ctx == null){
            return null;
        }
        ProposalValidator proposalValidator = ProposalValidator.getInstance();

        //execute business level validation
        boolean validate = proposalValidator.validate(ctx);
        ctx.setValidationSuccess(validate);
        return ctx;
    }
}
