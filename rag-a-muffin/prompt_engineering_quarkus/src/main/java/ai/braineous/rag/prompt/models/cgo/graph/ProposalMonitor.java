package ai.braineous.rag.prompt.models.cgo.graph;

import java.util.HashSet;
import java.util.Set;

public class ProposalMonitor {
    private ProposalValidator proposalValidator = new ProposalValidator();

    public ProposalContext receive(ProposalContext ctx){
        //execute business level validation
        boolean validate = this.proposalValidator.validate(ctx);
        ctx.setValidationSuccess(validate);
        return ctx;
    }
}
