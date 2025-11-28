package splat.executor;

import splat.semanticanalyzer.Type;

public class Value {
    private final Type type;
    private final Object value;

    public Value(Type type, Object value) {
        this.type = type;
        this.value = value;
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
        if (!isInteger()) {
            throw new IllegalStateException("Not an integer value");
        }
        return (Integer) value;
    }

    public boolean asBoolean() {
        if (!isBoolean()) {
            throw new IllegalStateException("Not a boolean value");
        }
        return (Boolean) value;
    }

    public String asString() {
        if (!isString()) {
            throw new IllegalStateException("Not a string value");
        }
        return (String) value;
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
        if (value == null) {
            return "null";
        }
        return value.toString();
    }
}
