package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.Value;
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

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap,
                          Map<String, Value> varAndParamMap) throws ExecutionException {
        Value leftVal = left.evaluate(funcMap, varAndParamMap);
        Value rightVal = right.evaluate(funcMap, varAndParamMap);
        String opLexeme = op.getLexeme();

        switch (opLexeme) {
            case "+":
                return Value.ofInteger(leftVal.asInteger() + rightVal.asInteger());
            case "-":
                return Value.ofInteger(leftVal.asInteger() - rightVal.asInteger());
            case "*":
                return Value.ofInteger(leftVal.asInteger() * rightVal.asInteger());
            case "/":
                if (rightVal.asInteger() == 0) {
                    throw new ExecutionException("Division by zero", op.getLine(), op.getCol());
                }
                return Value.ofInteger(leftVal.asInteger() / rightVal.asInteger());
            case "%":
                if (rightVal.asInteger() == 0) {
                    throw new ExecutionException("Division by zero", op.getLine(), op.getCol());
                }
                return Value.ofInteger(leftVal.asInteger() % rightVal.asInteger());
            case "and":
                return Value.ofBoolean(leftVal.asBoolean() && rightVal.asBoolean());
            case "or":
                return Value.ofBoolean(leftVal.asBoolean() || rightVal.asBoolean());
            case "<":
                return Value.ofBoolean(leftVal.asInteger() < rightVal.asInteger());
            case "<=":
                return Value.ofBoolean(leftVal.asInteger() <= rightVal.asInteger());
            case ">":
                return Value.ofBoolean(leftVal.asInteger() > rightVal.asInteger());
            case ">=":
                return Value.ofBoolean(leftVal.asInteger() >= rightVal.asInteger());
            case "==":
                return Value.ofBoolean(equalsValues(leftVal, rightVal));
            case "!=":
                return Value.ofBoolean(!equalsValues(leftVal, rightVal));
            default:
                throw new ExecutionException("Unknown operator '" + opLexeme + "'", op.getLine(), op.getCol());
        }
    }

    private boolean equalsValues(Value leftVal, Value rightVal) {
        if (leftVal.getType() != rightVal.getType()) {
            return false;
        }
        switch (leftVal.getType()) {
            case INTEGER:
                return leftVal.asInteger() == rightVal.asInteger();
            case BOOLEAN:
                return leftVal.asBoolean() == rightVal.asBoolean();
            case STRING:
                return leftVal.asString().equals(rightVal.asString());
            default:
                return false;
        }
    }
}
