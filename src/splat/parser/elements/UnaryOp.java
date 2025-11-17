package splat.parser.elements;

import splat.lexer.Token;

public class UnaryOp extends Expression {
    private final Token op;
    private final Expression expr;

    public UnaryOp(Token op, Expression expr) {
        super(op);
        this.op = op;
        this.expr = expr;
    }

    public Token getOperator() {
        return op;
    }

    public Expression getExpr() {
        return expr;
    }

    @Override
    public String toString() {
        return "(" + op.getLexeme() + " " + expr + ")";
    }
}