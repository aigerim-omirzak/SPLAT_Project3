package splat.parser.elements;

import java.util.List;
import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Types;

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

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        String condType = condition.analyzeAndGetType(funcMap, varAndParamMap);
        if (!Types.BOOLEAN.equals(condType)) {
            throw new SemanticAnalysisException(
                    "While condition must be Boolean",
                    condition.getLine(), condition.getColumn());
        }

        for (Statement stmt : body) {
            stmt.analyze(funcMap, varAndParamMap);
        }
    }
}
