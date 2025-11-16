package splat.parser.elements;

import splat.lexer.Token;

public class ReturnStmt extends Statement {
    private Expression expr;

    public ReturnStmt(Token tok, Expression expr) {
        super(tok);
        this.expr = expr;
    }

    public Expression getExpr() {
        return expr;
    }

    public Token getReturnToken() {
        return super.getToken();
    }

    @Override
    public String toString() {
        return "return " + expr;
    }
}
