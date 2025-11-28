package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class ReturnStmt extends Statement {
    private Expression expr;

    public ReturnStmt(Token tok, Expression expr) {
        super(tok);
        this.expr = expr;
    }

    public Expression getExpr() {
        return expr;
    }

    public Token getReturnToken() {
        return super.getToken();
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap)
            throws SemanticAnalysisException {
        if (expr != null) {
            expr.analyzeAndGetType(funcMap, varAndParamMap);
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap)
            throws ReturnFromCall, ExecutionException {
        Value val = expr == null ? Value.voidValue() : expr.evaluate(funcMap, varAndParamMap);
        throw new ReturnFromCall(val);
    }

    @Override
    public String toString() {
        return "return " + expr;
    }
}
