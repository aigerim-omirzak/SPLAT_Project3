package splat.parser.elements;

import splat.lexer.Token;

public abstract class ASTElement {

    private final Token token;
    private final int line;
    private final int column;

    public ASTElement(Token tok) {
        this.token = tok;
        this.line = tok.getLine();
        this.column = tok.getCol();
    }

    public Token getToken() {
        return token;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
