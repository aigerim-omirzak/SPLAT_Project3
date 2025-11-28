package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class Assignment extends Statement {
    private final Token targetName;
    private final Expression assignedExpr;

    public Assignment(Token variable, Expression expr) {
        super(variable);
        this.targetName = variable;
        this.assignedExpr = expr;
    }

    public Token getVariable() { return targetName; }
    public Expression getExpression() { return assignedExpr; }

    @Override
    public String toString() {
        return targetName.getLexeme() + " := " + assignedExpr;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        String variableName = targetName.getLexeme();
        Type expectedType = varAndParamMap.get(variableName);
        ensureVariableExists(variableName, expectedType);

        Type expressionType = assignedExpr.analyzeAndGetType(funcMap, varAndParamMap);
        if (expressionType != expectedType) {
            throw new SemanticAnalysisException(
                    "Type mismatch: cannot assign " + expressionType + " to " + expectedType,
                    targetName.getLine(), targetName.getCol());
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap,
                        Map<String, Value> varAndParamMap) throws ExecutionException {
        String variableName = targetName.getLexeme();
        ensureVariableIsDefined(varAndParamMap, variableName);

        Value value = assignedExpr.evaluate(funcMap, varAndParamMap);
        varAndParamMap.put(variableName, value);
    }

    private void ensureVariableExists(String variableName, Type existingType) throws SemanticAnalysisException {
        if (existingType == null) {
            throw new SemanticAnalysisException(
                    "Variable '" + variableName + "' is not defined",
                    targetName.getLine(), targetName.getCol());
        }
    }

    private void ensureVariableIsDefined(Map<String, Value> varAndParamMap, String variableName) throws ExecutionException {
        if (!varAndParamMap.containsKey(variableName)) {
            throw new ExecutionException(
                    "Variable '" + variableName + "' is not defined",
                    targetName.getLine(), targetName.getCol());
        }
    }
}
