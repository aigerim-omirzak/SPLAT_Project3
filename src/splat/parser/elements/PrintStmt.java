package splat.parser.elements;

import splat.lexer.Token;

public class PrintStmt extends Statement {
    private Expression expr;

    public PrintStmt(Token tok, Expression expr) {
        super(tok);
        this.expr = expr;
    }


    public Token getStartToken() {
        return super.getStartToken();
    }

    @Override
    public String toString() {
        return "print " + expr;
    }
}
