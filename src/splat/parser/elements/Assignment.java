package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class Assignment extends Statement {
    private Token variable;   // должен быть Token
    private Expression expr;

    public Assignment(Token variable, Expression expr) {
        super(variable);
        this.variable = variable;
        this.expr = expr;
    }

    public Token getVariable() { return variable; }
    public Expression getExpression() { return expr; }

    @Override
    public String toString() {
        return variable.getLexeme() + " := " + expr;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        String name = variable.getLexeme();
        Type targetType = varAndParamMap.get(name);
        if (targetType == null) {
            throw new SemanticAnalysisException(
                    "Variable '" + name + "' is not defined",
                    variable.getLine(), variable.getCol());
        }

        Type exprType = expr.analyzeAndGetType(funcMap, varAndParamMap);
        if (exprType != targetType) {
            throw new SemanticAnalysisException(
                    "Type mismatch: cannot assign " + exprType + " to " + targetType,
                    variable.getLine(), variable.getCol());
        }
    }
}

