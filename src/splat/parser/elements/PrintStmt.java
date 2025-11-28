package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

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
                        Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
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

        Type type = expr.analyzeAndGetType(funcMap, varAndParamMap);
        if (!isPrintable(type)) {
            throw new SemanticAnalysisException(
                    "Cannot print expressions of type " + type,
                    expr.getLine(), expr.getColumn());
        }

        if (isPrintLine && type == Type.VOID) {
            throw new SemanticAnalysisException(
                    "print_line cannot print void expressions",
                    expr.getLine(), expr.getColumn());
        }
    }

    private boolean isPrintable(Type type) {
        return type == Type.INTEGER || type == Type.STRING || type == Type.BOOLEAN;
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap,
                        Map<String, Value> varAndParamMap) throws ExecutionException {
        boolean isPrintLine = "print_line".equals(getStartToken().getLexeme());
        if (expr != null) {
            Value value = expr.evaluate(funcMap, varAndParamMap);
            if (isPrintLine) {
                System.out.println(value.toString());
            } else {
                System.out.print(value.toString());
            }
        } else {
            System.out.println();
        }
    }
}
