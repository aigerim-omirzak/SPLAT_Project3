package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Types;

public class FunctionCallStmt extends Statement {
    private final FunctionCall call;

    public FunctionCallStmt(FunctionCall call) {
        super(call.getStartToken());
        this.call = call;
    }

    public FunctionCall getCall() {
        return call;
    }



    public Token getStartToken() {
        return call.getStartToken();
    }


    @Override
    public String toString() {
        return "FunctionCallStmt(" + call + ")";
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        String returnType = call.analyzeCall(funcMap, varAndParamMap);
        if (!Types.VOID.equals(returnType)) {
            Token start = call.getStartToken();
            throw new SemanticAnalysisException(
                    "Function '" + start.getLexeme() + "' returns " + returnType
                            + " and cannot be used as a statement",
                    start.getLine(), start.getCol());
        }
    }
}
