package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class PrintStmt extends Statement {
    private final Expression expr;

    public PrintStmt(Token tok, Expression expr) {
        super(tok);
        this.expr = expr;
    }

    public Expression getExpr() { return expr; }

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
        boolean isPrintLine = isPrintLine();

        if (expr == null) {
            requirePrintLineForEmptyExpression(isPrintLine);
            return;
        }

        Type expressionType = expr.analyzeAndGetType(funcMap, varAndParamMap);
        ensurePrintableType(expressionType);

        if (isPrintLine && expressionType == Type.VOID) {
            throw new SemanticAnalysisException(
                    "print_line cannot print void expressions",
                    expr.getLine(), expr.getColumn());
        }
    }

    private boolean isPrintable(Type type) {
        return type == Type.INTEGER || type == Type.STRING || type == Type.BOOLEAN;
    }

    private boolean isPrintLine() {
        return "print_line".equals(getStartToken().getLexeme());
    }

    private void requirePrintLineForEmptyExpression(boolean isPrintLine) throws SemanticAnalysisException {
        if (!isPrintLine) {
            throw new SemanticAnalysisException(
                    "print requires an expression",
                    getLine(), getColumn());
        }
    }

    private void ensurePrintableType(Type expressionType) throws SemanticAnalysisException {
        if (!isPrintable(expressionType)) {
            throw new SemanticAnalysisException(
                    "Cannot print expressions of type " + expressionType,
                    expr.getLine(), expr.getColumn());
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap,
                        Map<String, Value> varAndParamMap) throws ExecutionException {
        boolean isPrintLine = isPrintLine();
        if (expr == null) {
            System.out.println();
            return;
        }

        Value value = expr.evaluate(funcMap, varAndParamMap);
        if (isPrintLine) {
            System.out.println(value.toString());
        } else {
            System.out.print(value.toString());
        }
    }
}
