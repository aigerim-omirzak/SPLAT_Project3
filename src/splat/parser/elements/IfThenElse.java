package splat.parser.elements;

import splat.lexer.Token;
import java.util.List;

public class IfThenElse extends Statement {
    private Expression condition;
    private List<Statement> thenStmts;
    private List<Statement> elseStmts;
    private Token ifToken;

    public IfThenElse(Token tok, Expression condition,
                      List<Statement> thenStmts, List<Statement> elseStmts) {
        super(tok);
        this.ifToken = tok;
        this.condition = condition;
        this.thenStmts = thenStmts;
        this.elseStmts = elseStmts;
    }

    public Token getIfToken() {
        return ifToken;
    }

    public Expression getCondition() { return condition; }
    public List<Statement> getThenStmts() { return thenStmts; }
    public List<Statement> getElseStmts() { return elseStmts; }
}
