package splat.executor;

import splat.semanticanalyzer.Type;

public class Value {
    private final Type type;
    private final Object value;

    public Value(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static Value ofInteger(int v) {
        return new Value(Type.INTEGER, v);
    }

    public static Value ofBoolean(boolean v) {
        return new Value(Type.BOOLEAN, v);
    }

    public static Value ofString(String v) {
        return new Value(Type.STRING, v);
    }

    public static Value voidValue() {
        return new Value(Type.VOID, null);
    }

    public Type getType() {
        return type;
    }

    public Object getRaw() {
        return value;
    }

    public int asInt() {
        return (Integer) value;
    }

    public boolean asBoolean() {
        return (Boolean) value;
    }

    public String asString() {
        return (String) value;
    }
}

