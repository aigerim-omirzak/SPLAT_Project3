package splat.executor;

public class ReturnFromCall extends Exception {
    private static final long serialVersionUID = 1L;

    private final Value value;

    public ReturnFromCall(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    /**
     * Convenience accessor mirroring {@link #getValue()} to avoid IDE
     * resolution issues when catching this exception in expression execution.
     */
    public Value value() {
        return value;
    }
}

