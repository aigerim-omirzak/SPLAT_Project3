package splat.executor;

import splat.semanticanalyzer.Type;

public class Value {
    private final Type type;
    private final Object payload;

    public Value(Type type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public Type getType() {
        return type;
    }

    public boolean isInteger() {
        return type == Type.INTEGER;
    }

    public boolean isBoolean() {
        return type == Type.BOOLEAN;
    }

    public boolean isString() {
        return type == Type.STRING;
    }

    public int asInteger() {
        ensureType(Type.INTEGER, "Not an integer value");
        return (Integer) payload;
    }

    public boolean asBoolean() {
        ensureType(Type.BOOLEAN, "Not a boolean value");
        return (Boolean) payload;
    }

    public String asString() {
        ensureType(Type.STRING, "Not a string value");
        return (String) payload;
    }

    public static Value defaultValue(Type type) {
        switch (type) {
            case INTEGER:
                return new Value(Type.INTEGER, 0);
            case BOOLEAN:
                return new Value(Type.BOOLEAN, false);
            case STRING:
                return new Value(Type.STRING, "");
            case VOID:
            default:
                return null;
        }
    }

    public static Value ofInteger(int value) {
        return new Value(Type.INTEGER, value);
    }

    public static Value ofBoolean(boolean value) {
        return new Value(Type.BOOLEAN, value);
    }

    public static Value ofString(String value) {
        return new Value(Type.STRING, value);
    }

    @Override
    public String toString() {
        return String.valueOf(payload);
    }

    private void ensureType(Type expected, String message) {
        if (type != expected) {
            throw new IllegalStateException(message);
        }
    }
}
