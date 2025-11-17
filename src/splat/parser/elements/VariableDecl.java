package splat.parser.elements;

import splat.lexer.Token;

public class VariableDecl extends Declaration {
    private final Token type;

    public VariableDecl(Token name, Token type) {
        super(name);
        this.type = type;
    }

    public Token getName() {
        return getLabel();
    }

    public Token getType() {
        return type;
    }


    public Token getStartToken() {
        return getLabel();
    }

    @Override
    public String toString() {
        return String.format("VarDecl(name=%s, type=%s)", getName().getLexeme(), type.getLexeme());
    }
}
