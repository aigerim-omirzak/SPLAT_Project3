package splat.parser.elements;

import splat.lexer.Token;

public class Literal extends Expression {
    private String value;

    public Literal(Token token) {
        super(token);
        this.value = token.getLexeme();
    }

    public String getValue() {
        return value;
    }

    public String getStringValue() {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public boolean isStringLiteral() {
        return value.startsWith("\"") && value.endsWith("\"");
    }

    public boolean isIntegerLiteral() {
        return value.matches("\\d+");
    }

    public boolean isBooleanLiteral() {
        return value.equals("true") || value.equals("false");
    }
}