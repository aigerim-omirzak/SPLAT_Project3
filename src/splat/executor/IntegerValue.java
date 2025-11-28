package splat.executor;

import splat.semanticanalyzer.Type;

public class IntegerValue extends Value {
    private final int value;

    public IntegerValue(int value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.INTEGER;
    }

    @Override
    public int asInteger() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
