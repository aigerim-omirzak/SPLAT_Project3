package splat.parser.elements;

import splat.lexer.Token;
import java.util.List;

public class WhileLoop extends Statement {
    private Expression condition;
    private List<Statement> body;
    private Token whileToken;

    public WhileLoop(Token tok, Expression condition, List<Statement> body) {
        super(tok);
        this.whileToken = tok;
        this.condition = condition;
        this.body = body;
    }

    public Token getWhileToken() {
        return whileToken;
    }

    public Expression getCondition() { return condition; }
    public List<Statement> getBody() { return body; }
}
