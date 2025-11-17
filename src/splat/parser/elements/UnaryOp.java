package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Types;

public class UnaryOp extends Expression {
    private final Token op;
    private final Expression expr;

    public UnaryOp(Token op, Expression expr) {
        super(op);
        this.op = op;
        this.expr = expr;
    }

    public Token getOperator() {
        return op;
    }

    public Expression getExpr() {
        return expr;
    }

    @Override
    public String toString() {
        return "(" + op.getLexeme() + " " + expr + ")";
    }

    @Override
    public String analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                    Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        String childType = expr.analyzeAndGetType(funcMap, varAndParamMap);
        String opLexeme = op.getLexeme();

        if ("-".equals(opLexeme)) {
            if (!Types.INTEGER.equals(childType)) {
                throw new SemanticAnalysisException(
                        "Unary '-' requires an integer operand",
                        op.getLine(), op.getCol());
            }
            return Types.INTEGER;
        }

        if ("not".equals(opLexeme)) {
            if (!Types.BOOLEAN.equals(childType)) {
                throw new SemanticAnalysisException(
                        "'not' requires a boolean operand",
                        op.getLine(), op.getCol());
            }
            return Types.BOOLEAN;
        }

        throw new SemanticAnalysisException(
                "Unknown unary operator '" + opLexeme + "'",
                op.getLine(), op.getCol());
    }
}