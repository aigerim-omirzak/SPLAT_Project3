package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

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
                        Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        String name = variable.getLexeme();
        String targetType = varAndParamMap.get(name);
        if (targetType == null) {
            throw new SemanticAnalysisException(
                    "Variable '" + name + "' is not defined",
                    variable.getLine(), variable.getCol());
        }

        String exprType = expr.analyzeAndGetType(funcMap, varAndParamMap);
        if (!exprType.equals(targetType)) {
            throw new SemanticAnalysisException(
                    "Type mismatch: cannot assign " + exprType + " to " + targetType,
                    variable.getLine(), variable.getCol());
        }
    }
}

