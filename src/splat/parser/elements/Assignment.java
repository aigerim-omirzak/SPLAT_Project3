package splat.parser.elements;

import splat.lexer.Token;

public class Assignment extends Statement {
    private Token variable;
    private Expression expr;

    public Assignment(Token variable, Expression expr) {
        super(variable);
        this.variable = variable;
        this.expr = expr;
    }

    public Expression getExpression() { return expr; }

    @Override
    public String toString() {
        return variable.getLexeme() + " := " + expr;
    }
}

