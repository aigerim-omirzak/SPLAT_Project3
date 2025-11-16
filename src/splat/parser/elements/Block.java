package splat.parser.elements;

import java.util.List;
import splat.lexer.Token;

public class Block extends Statement {
    private List<Statement> statements;

    public Block(Token tok, List<Statement> stmts) {
        super(tok);
        this.statements = stmts;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public Token getStartToken() {
        return super.getStartToken();
    }
}
