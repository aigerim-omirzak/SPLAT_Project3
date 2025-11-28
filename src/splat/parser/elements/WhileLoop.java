package splat.parser.elements;

import java.util.List;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class WhileLoop extends Statement {
    private Expression condition;
    private List<Statement> body;
    private Token whileToken;

    public WhileLoop(Token tok, Expression condition, List<Statement> body) {
        super(tok);
        this.whileToken = tok;
        this.condition = condition;
        this.body = body;
    }

    public Token getWhileToken() {
        return whileToken;
    }

    public Expression getCondition() { return condition; }
    public List<Statement> getBody() { return body; }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type condType = condition.analyzeAndGetType(funcMap, varAndParamMap);
        if (condType != Type.BOOLEAN) {
            throw new SemanticAnalysisException(
                    "While condition must be Boolean",
                    condition.getLine(), condition.getColumn());
        }

        for (Statement stmt : body) {
            stmt.analyze(funcMap, varAndParamMap);
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap,
                        Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        while (true) {
            Value condVal = condition.evaluate(funcMap, varAndParamMap);
            if (!condVal.isBoolean()) {
                throw new ExecutionException("While condition must be Boolean", condition.getLine(), condition.getColumn());
            }
            if (!condVal.asBoolean()) {
                break;
            }
            for (Statement stmt : body) {
                stmt.execute(funcMap, varAndParamMap);
            }
        }
    }
}
