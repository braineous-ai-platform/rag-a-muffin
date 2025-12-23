package ai.braineous.rag.prompt.models.cgo.graph.mutation;

class MutationResultListenerImpl implements MutationResultListener{
    private MutationResult mutationResult;

    public MutationResultListenerImpl() {
    }

    public MutationResult getMutationResult() {
        return mutationResult;
    }

    public void setMutationResult(MutationResult mutationResult) {
        this.mutationResult = mutationResult;
    }

    @Override
    public MutationResult result() {
        return this.mutationResult;
    }

    @Override
    public String toString() {
        return "MutationResultListenerImpl{" +
                "mutationResult=" + mutationResult +
                '}';
    }
}
