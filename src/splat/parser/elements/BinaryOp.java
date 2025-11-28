package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

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
    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                  Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type leftType = left.analyzeAndGetType(funcMap, varAndParamMap);
        Type rightType = right.analyzeAndGetType(funcMap, varAndParamMap);
        String opLexeme = op.getLexeme();

        switch (opLexeme) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
                ensureIntegerOperands(leftType, rightType);
                return Type.INTEGER;
            case "and":
            case "or":
                ensureBooleanOperands(leftType, rightType);
                return Type.BOOLEAN;
            case "<":
            case "<=":
            case ">":
            case ">=":
                ensureIntegerOperands(leftType, rightType);
                return Type.BOOLEAN;
            case "==":
            case "!=":
                ensureComparableOperands(leftType, rightType);
                return Type.BOOLEAN;
            default:
                throw new SemanticAnalysisException(
                        "Unknown binary operator '" + opLexeme + "'",
                        op.getLine(), op.getCol());
        }
    }

    private void ensureIntegerOperands(Type leftType, Type rightType) throws SemanticAnalysisException {
        if (leftType != Type.INTEGER || rightType != Type.INTEGER) {
            throw new SemanticAnalysisException(
                    "Operator '" + op.getLexeme() + "' requires integer operands",
                    op.getLine(), op.getCol());
        }
    }

    private void ensureBooleanOperands(Type leftType, Type rightType) throws SemanticAnalysisException {
        if (leftType != Type.BOOLEAN || rightType != Type.BOOLEAN) {
            throw new SemanticAnalysisException(
                    "Operator '" + op.getLexeme() + "' requires boolean operands",
                    op.getLine(), op.getCol());
        }
    }

    private void ensureComparableOperands(Type leftType, Type rightType) throws SemanticAnalysisException {
        if (leftType == Type.VOID || rightType == Type.VOID || leftType != rightType) {
            throw new SemanticAnalysisException(
                    "Equality operator requires operands of the same non-void type",
                    op.getLine(), op.getCol());
        }
    }
}