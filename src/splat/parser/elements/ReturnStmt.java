package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class ReturnStmt extends Statement {
    private final Expression expr;

    public ReturnStmt(Token tok, Expression expr) {
        super(tok);
        this.expr = expr;
    }

    public Expression getExpr() { return expr; }

    public Token getReturnToken() {
        return super.getToken();
    }

    @Override
    public String toString() {
        return "return " + expr;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type expected = varAndParamMap.get(Statement.RETURN_TYPE_SLOT);
        ensureInsideFunction(expected);

        if (expr == null) {
            ensureVoidReturn(expected);
            return;
        }

        Type actual = expr.analyzeAndGetType(funcMap, varAndParamMap);
        if (expected == Type.VOID) {
            throw new SemanticAnalysisException(
                    "Void functions cannot return a value",
                    expr.getLine(), expr.getColumn());
        }
        if (actual != expected) {
            throw new SemanticAnalysisException(
                    "Return type mismatch: expected " + expected + " but found " + actual,
                    expr.getLine(), expr.getColumn());
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap,
                        Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        if (expr == null) {
            throw new ReturnFromCall(null);
        }
        Value value = expr.evaluate(funcMap, varAndParamMap);
        throw new ReturnFromCall(value);
    }

    private void ensureInsideFunction(Type expected) throws SemanticAnalysisException {
        if (expected == null) {
            throw new SemanticAnalysisException(
                    "Return statement not allowed outside of a function",
                    getReturnToken().getLine(), getReturnToken().getCol());
        }
    }

    private void ensureVoidReturn(Type expected) throws SemanticAnalysisException {
        if (expected != Type.VOID) {
            throw new SemanticAnalysisException(
                    "Return statement requires an expression of type " + expected,
                    getReturnToken().getLine(), getReturnToken().getCol());
        }
    }
}
