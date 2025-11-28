package splat.executor;

public class ReturnFromCall extends Exception {
    private final Value value;

    public ReturnFromCall(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }
}

