package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.BooleanValue;
import splat.executor.IntegerValue;
import splat.executor.StringValue;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class Literal extends Expression {
    private String value;

    public Literal(Token token) {
        super(token);
        this.value = token.getLexeme();
    }

    public String getValue() {
        return value;
    }

    // If you need to get the actual value without quotes for strings
    public String getStringValue() {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public boolean isStringLiteral() {
        return value.startsWith("\"") && value.endsWith("\"");
    }

    public boolean isIntegerLiteral() {
        return value.matches("\\d+");
    }

    public boolean isBooleanLiteral() {
        return value.equals("true") || value.equals("false");
    }

    @Override
    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                  Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        if (isIntegerLiteral()) {
            return Type.INTEGER;
        }
        if (isBooleanLiteral()) {
            return Type.BOOLEAN;
        }
        if (isStringLiteral()) {
            return Type.STRING;
        }

        throw new SemanticAnalysisException(
                "Unknown literal '" + value + "'",
                getLine(), getColumn());
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap,
                          Map<String, Value> varAndParamMap) throws ExecutionException {
        if (isIntegerLiteral()) {
            return new IntegerValue(Integer.parseInt(value));
        }
        if (isBooleanLiteral()) {
            return new BooleanValue(Boolean.parseBoolean(value));
        }
        if (isStringLiteral()) {
            return new StringValue(getStringValue());
        }
        throw new ExecutionException("Unknown literal '" + value + "'", getLine(), getColumn());
    }
}
