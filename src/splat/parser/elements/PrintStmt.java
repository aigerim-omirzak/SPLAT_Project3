package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Types;

public class PrintStmt extends Statement {
    private Expression expr;

    public PrintStmt(Token tok, Expression expr) {
        super(tok);
        this.expr = expr;
    }

    public Expression getExpr() {
        return expr;
    }

    public Token getStartToken() {
        return super.getStartToken();
    }

    @Override
    public String toString() {
        return "print " + expr;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        String keyword = getStartToken().getLexeme();
        boolean isPrintLine = "print_line".equals(keyword);

        if (expr == null) {
            if (!isPrintLine) {
                throw new SemanticAnalysisException(
                        "print requires an expression",
                        getLine(), getColumn());
            }
            return;
        }

        String type = expr.analyzeAndGetType(funcMap, varAndParamMap);
        if (!isPrintable(type)) {
            throw new SemanticAnalysisException(
                    "Cannot print expressions of type " + type,
                    expr.getLine(), expr.getColumn());
        }

        if (isPrintLine && Types.VOID.equals(type)) {
            throw new SemanticAnalysisException(
                    "print_line cannot print void expressions",
                    expr.getLine(), expr.getColumn());
        }
    }

    private boolean isPrintable(String type) {
        return Types.INTEGER.equals(type) || Types.STRING.equals(type) || Types.BOOLEAN.equals(type);
    }
}
