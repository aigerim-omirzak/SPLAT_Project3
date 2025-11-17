package splat.parser.elements;

import java.util.List;
import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Types;

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

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        String condType = condition.analyzeAndGetType(funcMap, varAndParamMap);
        if (!Types.BOOLEAN.equals(condType)) {
            throw new SemanticAnalysisException(
                    "If condition must be Boolean",
                    condition.getLine(), condition.getColumn());
        }

        for (Statement stmt : thenStmts) {
            stmt.analyze(funcMap, varAndParamMap);
        }
        for (Statement stmt : elseStmts) {
            stmt.analyze(funcMap, varAndParamMap);
        }
    }
}
