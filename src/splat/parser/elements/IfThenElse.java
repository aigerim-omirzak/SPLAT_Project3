package splat.parser.elements;

import java.util.List;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class IfThenElse extends Statement {
    private final Expression condition;
    private final List<Statement> thenStmts;
    private final List<Statement> elseStmts;
    private final Token ifToken;

    public IfThenElse(Token tok, Expression condition,
                      List<Statement> thenStmts, List<Statement> elseStmts) {
        super(tok);
        this.ifToken = tok;
        this.condition = condition;
        this.thenStmts = thenStmts;
        this.elseStmts = elseStmts;
    }

    public Token getIfToken() {
        return ifToken;
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getThenStmts() {
        return thenStmts;
    }

    public List<Statement> getElseStmts() {
        return elseStmts;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        validateConditionType(funcMap, varAndParamMap);
        analyzeBranch(funcMap, varAndParamMap, thenStmts);
        analyzeBranch(funcMap, varAndParamMap, elseStmts);
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap,
                        Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        Value condVal = condition.evaluate(funcMap, varAndParamMap);
        ensureBooleanCondition(condVal);
        executeBranch(funcMap, varAndParamMap, condVal.asBoolean() ? thenStmts : elseStmts);
    }

    private void validateConditionType(Map<String, FunctionDecl> funcMap,
                                       Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type condType = condition.analyzeAndGetType(funcMap, varAndParamMap);
        if (condType != Type.BOOLEAN) {
            throw new SemanticAnalysisException(
                    "If condition must be Boolean",
                    condition.getLine(),
                    condition.getColumn());
        }
    }

    private void analyzeBranch(Map<String, FunctionDecl> funcMap,
                               Map<String, Type> varAndParamMap,
                               List<Statement> branchStatements) throws SemanticAnalysisException {
        for (Statement stmt : branchStatements) {
            stmt.analyze(funcMap, varAndParamMap);
        }
    }

    private void ensureBooleanCondition(Value condVal) throws ExecutionException {
        if (!condVal.isBoolean()) {
            throw new ExecutionException("If condition must be Boolean", condition.getLine(), condition.getColumn());
        }
    }

    private void executeBranch(Map<String, FunctionDecl> funcMap,
                               Map<String, Value> varAndParamMap,
                               List<Statement> branchStatements) throws ReturnFromCall, ExecutionException {
        for (Statement stmt : branchStatements) {
            stmt.execute(funcMap, varAndParamMap);
        }
    }
}
