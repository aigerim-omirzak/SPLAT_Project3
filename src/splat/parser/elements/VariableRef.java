package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

public class VariableRef extends Expression {
    private final Token name;

    public VariableRef(Token name) {
        super(name);
        this.name = name;
    }

    public Token getName() { return name; }

    @Override
    public String toString() {
        return name.getLexeme();
    }

    @Override
    public String analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                    Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        String lexeme = name.getLexeme();
        String type = varAndParamMap.get(lexeme);
        if (type == null) {
            throw new SemanticAnalysisException(
                    "Variable '" + lexeme + "' is not defined",
                    name.getLine(), name.getCol());
        }
        return type;
    }
}
