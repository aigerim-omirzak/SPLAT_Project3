package splat.parser.elements;

import splat.lexer.Token;

public class VariableDecl extends Declaration {
    private final Token typeToken;

    public VariableDecl(Token name, Token type) {
        super(name);
        this.typeToken = type;
    }

    public Token getName() {
        return getLabel();
    }

    public Token getType() {
        return typeToken;
    }

    public Token getStartToken() {
        return getLabel();
    }

    @Override
    public String toString() {
        return String.format(
                "VarDecl(name=%s, type=%s)",
                getLabel().getLexeme(),
                typeToken.getLexeme()
        );
    }
}
