package splat.parser.elements;

import java.util.Map;

import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Types;

public class ExpressionStmt extends Statement {
    private Expression expr;

    public ExpressionStmt(Expression expr) {
        super(expr.getToken());
        this.expr = expr;
    }

    public Expression getExpression() {
        return expr;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        String type = expr.analyzeAndGetType(funcMap, varAndParamMap);
        if (Types.VOID.equals(type)) {
            throw new SemanticAnalysisException(
                    "Expression statement cannot be void",
                    expr.getLine(), expr.getColumn());
        }
    }
}