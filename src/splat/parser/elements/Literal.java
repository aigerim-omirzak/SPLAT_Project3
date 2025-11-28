package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class Literal extends Expression {
    private final String lexeme;

    public Literal(Token token) {
        super(token);
        this.lexeme = token.getLexeme();
    }

    public String getValue() {
        return lexeme;
    }

    public String getStringValue() {
        if (isStringLiteral()) {
            return lexeme.substring(1, lexeme.length() - 1);
        }
        return lexeme;
    }

    public boolean isStringLiteral() {
        return lexeme.startsWith("\"") && lexeme.endsWith("\"");
    }

    public boolean isIntegerLiteral() {
        return lexeme.matches("\\d+");
    }

    public boolean isBooleanLiteral() {
        return "true".equals(lexeme) || "false".equals(lexeme);
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
                "Unknown literal '" + lexeme + "'",
                getLine(), getColumn());
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap,
                          Map<String, Value> varAndParamMap) throws ExecutionException {
        if (isIntegerLiteral()) {
            return Value.ofInteger(Integer.parseInt(lexeme));
        }
        if (isBooleanLiteral()) {
            return Value.ofBoolean(Boolean.parseBoolean(lexeme));
        }
        if (isStringLiteral()) {
            return Value.ofString(getStringValue());
        }
        throw new ExecutionException("Unknown literal '" + lexeme + "'", getLine(), getColumn());
    }
}
