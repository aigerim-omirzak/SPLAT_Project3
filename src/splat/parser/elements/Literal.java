package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Types;

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
    public String analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                    Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        if (isIntegerLiteral()) {
            return Types.INTEGER;
        }
        if (isBooleanLiteral()) {
            return Types.BOOLEAN;
        }
        if (isStringLiteral()) {
            return Types.STRING;
        }

        throw new SemanticAnalysisException(
                "Unknown literal '" + value + "'",
                getLine(), getColumn());
    }
}