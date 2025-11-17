package splat.parser.elements;

import splat.lexer.Token;

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
}
