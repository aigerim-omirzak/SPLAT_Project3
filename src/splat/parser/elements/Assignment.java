package splat.parser.elements;

import splat.lexer.Token;

public class Assignment extends Statement {
    private Token variable;   // должен быть Token
    private Expression expr;

    public Assignment(Token variable, Expression expr) {
        super(variable);
        this.variable = variable;
        this.expr = expr;
    }

    public Token getVariable() { return variable; }
    public Expression getExpression() { return expr; }

    @Override
    public String toString() {
        return variable.getLexeme() + " := " + expr;
    }
}

