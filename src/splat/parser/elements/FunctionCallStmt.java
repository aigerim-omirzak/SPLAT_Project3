package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class FunctionCallStmt extends Statement {
    private final FunctionCall call;

    public FunctionCallStmt(FunctionCall call) {
        super(call.getStartToken());
        this.call = call;
    }

    @Override
    public String toString() {
        return "FunctionCallStmt(" + call + ")";
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type returnType = call.analyzeCall(funcMap, varAndParamMap);
        ensureVoidCall(returnType, call.getStartToken());
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap,
                        Map<String, Value> varAndParamMap) throws ExecutionException {
        call.evaluate(funcMap, varAndParamMap);
    }

    private void ensureVoidCall(Type returnType, Token startToken) throws SemanticAnalysisException {
        if (returnType != Type.VOID) {
            throw new SemanticAnalysisException(
                    "Function '" + startToken.getLexeme() + "' returns " + returnType
                            + " and cannot be used as a statement",
                    startToken.getLine(), startToken.getCol());
        }
    }
}
