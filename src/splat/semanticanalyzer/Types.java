package splat.semanticanalyzer;

import splat.lexer.Token;

/**
 * Simple utility holder for SPLAT type names.
 */
public final class Types {
    public static final String INTEGER = "Integer";
    public static final String BOOLEAN = "Boolean";
    public static final String STRING = "String";
    public static final String VOID = "void";

    private Types() {
    }

    public static String fromToken(Token token) throws SemanticAnalysisException {
        if (token == null) {
            return VOID;
        }
        return fromLexeme(token.getLexeme(), token.getLine(), token.getCol());
    }

    public static String fromLexeme(String lexeme, int line, int column) throws SemanticAnalysisException {
        if (lexeme == null) {
            throw new SemanticAnalysisException("Unknown type", line, column);
        }

        switch (lexeme.toLowerCase()) {
            case "integer":
                return INTEGER;
            case "boolean":
                return BOOLEAN;
            case "string":
                return STRING;
            case "void":
                return VOID;
            default:
                throw new SemanticAnalysisException("Unknown type '" + lexeme + "'", line, column);
        }
    }
}
