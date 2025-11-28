package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.Value;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class ExpressionStmt extends Statement {
    private final Expression expr;

    public ExpressionStmt(Expression expr) {
        super(expr.getToken());
        this.expr = expr;
    }

    public Expression getExpression() {
        return expr;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type type = expr.analyzeAndGetType(funcMap, varAndParamMap);
        enforceNonVoid(type);
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap,
                        Map<String, Value> varAndParamMap) throws ExecutionException {
        expr.evaluate(funcMap, varAndParamMap);
    }

    private void enforceNonVoid(Type type) throws SemanticAnalysisException {
        if (type == Type.VOID) {
            throw new SemanticAnalysisException(
                    "Expression statement cannot be void",
                    expr.getLine(),
                    expr.getColumn());
        }
    }
}
