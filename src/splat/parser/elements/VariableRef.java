package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

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
    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                  Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        String lexeme = name.getLexeme();
        Type type = varAndParamMap.get(lexeme);
        if (type == null) {
            throw new SemanticAnalysisException(
                    "Variable '" + lexeme + "' is not defined",
                    name.getLine(), name.getCol());
        }
        return type;
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap,
                          Map<String, Value> varAndParamMap) throws ExecutionException {
        Value value = varAndParamMap.get(name.getLexeme());
        if (value == null) {
            throw new ExecutionException("Variable '" + name.getLexeme() + "' has no value", name.getLine(), name.getCol());
        }
        return value;
    }
}
