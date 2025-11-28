package splat.executor;

import splat.semanticanalyzer.Type;

public abstract class Value {

    public abstract Type getType();

    public boolean isInteger() {
        return getType() == Type.INTEGER;
    }

    public boolean isBoolean() {
        return getType() == Type.BOOLEAN;
    }

    public boolean isString() {
        return getType() == Type.STRING;
    }

    public int asInteger() {
        throw new IllegalStateException("Not an integer value");
    }

    public boolean asBoolean() {
        throw new IllegalStateException("Not a boolean value");
    }

    public String asString() {
        throw new IllegalStateException("Not a string value");
    }

    public static Value defaultValue(Type type) {
        switch (type) {
            case INTEGER:
                return new IntegerValue(0);
            case BOOLEAN:
                return new BooleanValue(false);
            case STRING:
                return new StringValue("");
            case VOID:
            default:
                return null;
        }
    }
}
