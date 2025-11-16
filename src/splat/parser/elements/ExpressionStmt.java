package splat.parser.elements;

public class ExpressionStmt extends Statement {
    private Expression expr;

    public ExpressionStmt(Expression expr) {
        super(expr.getToken());
        this.expr = expr;
    }

    public Expression getExpression() {
        return expr;
    }
}