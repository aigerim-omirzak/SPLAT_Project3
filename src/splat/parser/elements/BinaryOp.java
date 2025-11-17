package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Types;

public class BinaryOp extends Expression {
    private final Expression left;
    private final Token op;
    private final Expression right;

    public BinaryOp(Expression left, Token op, Expression right) {
        super(op);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + left + " " + op.getLexeme() + " " + right + ")";
    }

    @Override
    public String analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                    Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        String leftType = left.analyzeAndGetType(funcMap, varAndParamMap);
        String rightType = right.analyzeAndGetType(funcMap, varAndParamMap);
        String opLexeme = op.getLexeme();

        switch (opLexeme) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
                ensureIntegerOperands(leftType, rightType);
                return Types.INTEGER;
            case "and":
            case "or":
                ensureBooleanOperands(leftType, rightType);
                return Types.BOOLEAN;
            case "<":
            case "<=":
            case ">":
            case ">=":
                ensureIntegerOperands(leftType, rightType);
                return Types.BOOLEAN;
            case "==":
            case "!=":
                ensureComparableOperands(leftType, rightType);
                return Types.BOOLEAN;
            default:
                throw new SemanticAnalysisException(
                        "Unknown binary operator '" + opLexeme + "'",
                        op.getLine(), op.getCol());
        }
    }

    private void ensureIntegerOperands(String leftType, String rightType) throws SemanticAnalysisException {
        if (!Types.INTEGER.equals(leftType) || !Types.INTEGER.equals(rightType)) {
            throw new SemanticAnalysisException(
                    "Operator '" + op.getLexeme() + "' requires integer operands",
                    op.getLine(), op.getCol());
        }
    }

    private void ensureBooleanOperands(String leftType, String rightType) throws SemanticAnalysisException {
        if (!Types.BOOLEAN.equals(leftType) || !Types.BOOLEAN.equals(rightType)) {
            throw new SemanticAnalysisException(
                    "Operator '" + op.getLexeme() + "' requires boolean operands",
                    op.getLine(), op.getCol());
        }
    }

    private void ensureComparableOperands(String leftType, String rightType) throws SemanticAnalysisException {
        if (Types.VOID.equals(leftType) || Types.VOID.equals(rightType) || !leftType.equals(rightType)) {
            throw new SemanticAnalysisException(
                    "Equality operator requires operands of the same non-void type",
                    op.getLine(), op.getCol());
        }
    }
}
