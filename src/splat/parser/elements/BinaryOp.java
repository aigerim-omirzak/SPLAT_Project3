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
        Value leftValue = left.evaluate(funcMap, varAndParamMap);
        Value rightValue = right.evaluate(funcMap, varAndParamMap);
        String opLexeme = op.getLexeme();

        switch (opLexeme) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
                return evaluateArithmetic(opLexeme, leftValue, rightValue);
            case "and":
            case "or":
                return evaluateBoolean(opLexeme, leftValue, rightValue);
            case "<":
            case "<=":
            case ">":
            case ">=":
                return evaluateComparison(opLexeme, leftValue, rightValue);
            case "==":
            case "!=":
                boolean valuesEqual = equalsValues(leftValue, rightValue);
                return Value.ofBoolean(opLexeme.equals("==") ? valuesEqual : !valuesEqual);
            default:
                throw new ExecutionException("Unknown operator '" + opLexeme + "'", op.getLine(), op.getCol());
        }
    }

    private Value evaluateArithmetic(String operator, Value leftValue, Value rightValue) throws ExecutionException {
        int leftInt = leftValue.asInteger();
        int rightInt = rightValue.asInteger();

        switch (operator) {
            case "+":
                return Value.ofInteger(leftInt + rightInt);
            case "-":
                return Value.ofInteger(leftInt - rightInt);
            case "*":
                return Value.ofInteger(leftInt * rightInt);
            case "/":
                ensureNonZeroDivisor(rightInt);
                return Value.ofInteger(leftInt / rightInt);
            case "%":
                ensureNonZeroDivisor(rightInt);
                return Value.ofInteger(leftInt % rightInt);
            default:
                throw new ExecutionException("Unknown operator '" + operator + "'", op.getLine(), op.getCol());
        }
    }

    private Value evaluateBoolean(String operator, Value leftValue, Value rightValue) {
        boolean leftBool = leftValue.asBoolean();
        boolean rightBool = rightValue.asBoolean();

        switch (operator) {
            case "and":
                return Value.ofBoolean(leftBool && rightBool);
            case "or":
                return Value.ofBoolean(leftBool || rightBool);
            default:
                throw new IllegalArgumentException("Unexpected boolean operator: " + operator);
        }
    }

    private Value evaluateComparison(String operator, Value leftValue, Value rightValue) {
        int leftInt = leftValue.asInteger();
        int rightInt = rightValue.asInteger();

        switch (operator) {
            case "<":
                return Value.ofBoolean(leftInt < rightInt);
            case "<=":
                return Value.ofBoolean(leftInt <= rightInt);
            case ">":
                return Value.ofBoolean(leftInt > rightInt);
            case ">=":
                return Value.ofBoolean(leftInt >= rightInt);
            default:
                throw new IllegalArgumentException("Unexpected comparison operator: " + operator);
        }
    }

    private void ensureNonZeroDivisor(int divisor) throws ExecutionException {
        if (divisor == 0) {
            throw new ExecutionException("Division by zero", op.getLine(), op.getCol());
        }
    }

    private boolean equalsValues(Value leftValue, Value rightValue) {
        if (leftValue.getType() != rightValue.getType()) {
            return false;
        }
        switch (leftValue.getType()) {
            case INTEGER:
                return leftValue.asInteger() == rightValue.asInteger();
            case BOOLEAN:
                return leftValue.asBoolean() == rightValue.asBoolean();
            case STRING:
                return leftValue.asString().equals(rightValue.asString());
            default:
                return false;
        }
    }
}
