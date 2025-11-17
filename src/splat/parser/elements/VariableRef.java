package splat.parser.elements;

import splat.lexer.Token;

public class VariableRef extends Expression {
    private final Token name;

    public VariableRef(Token name) {
        super(name);
        this.name = name;
    }

    public Token getName() { return name; }

    @Override
    public String toString() {
        return name.getLexeme();
    }
}
