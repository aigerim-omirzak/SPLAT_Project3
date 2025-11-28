package splat.parser.elements;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public class FunctionCall extends Expression {
    private final Token name;
    private final List<Expression> args;

    public FunctionCall(Token name, List<Expression> args) {
        super(name);
        this.name = name;
        this.args = args;
    }

    public Token getName() { return name; }
    public List<Expression> getArgs() { return args; }

    public Token getStartToken() {
        return name;
    }

    @Override
    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                  Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        FunctionDecl decl = funcMap.get(name.getLexeme());
        if (decl == null) {
            throw new SemanticAnalysisException("Unknown function '" + name.getLexeme() + "'", name.getLine(), name.getCol());
        }

        List<VariableDecl> params = decl.getParams();
        if (params.size() != args.size()) {
            throw new SemanticAnalysisException("Incorrect number of arguments for function", name.getLine(), name.getCol());
        }

        for (int i = 0; i < params.size(); i++) {
            Type expected = Type.fromToken(params.get(i).getType());
            Type actual = args.get(i).analyzeAndGetType(funcMap, varAndParamMap);
            if (expected != actual) {
                throw new SemanticAnalysisException("Argument type mismatch", args.get(i));
            }
        }

        return Type.fromToken(decl.getReturnType());
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap,
                          Map<String, Value> varAndParamMap) throws ExecutionException, ReturnFromCall {
        FunctionDecl decl = funcMap.get(name.getLexeme());
        if (decl == null) {
            throw new ExecutionException("Unknown function '" + name.getLexeme() + "'", getLine(), getColumn());
        }

        List<VariableDecl> params = decl.getParams();
        if (params.size() != args.size()) {
            throw new ExecutionException("Incorrect number of arguments for function", getLine(), getColumn());
        }

        Map<String, Value> newVarMap = new HashMap<>();

        for (int i = 0; i < params.size(); i++) {
            VariableDecl param = params.get(i);
            Value argVal = args.get(i).evaluate(funcMap, varAndParamMap);
            newVarMap.put(param.getLabelLexeme(), argVal);
        }

        for (VariableDecl local : decl.getLocalVars()) {
            try {
                Type type = Type.fromToken(local.getType());
                newVarMap.put(local.getLabelLexeme(), Value.defaultFor(type));
            } catch (SemanticAnalysisException sae) {
                throw new ExecutionException(sae.getMessage(), local.getLine(), local.getColumn());
            }
        }

        try {
            for (Statement stmt : decl.getStmts()) {
                stmt.execute(funcMap, newVarMap);
            }
        } catch (ReturnFromCall rfc) {
            syncGlobals(varAndParamMap, newVarMap, decl);
            return rfc.getValue();
        }

        syncGlobals(varAndParamMap, newVarMap, decl);

        try {
            return Value.defaultFor(Type.fromToken(decl.getReturnType()));
        } catch (SemanticAnalysisException sae) {
            throw new ExecutionException(sae.getMessage(), decl.getLine(), decl.getColumn());
        }
    }

    private void syncGlobals(Map<String, Value> callerMap, Map<String, Value> calleeMap, FunctionDecl decl) {
        java.util.Set<String> shadowed = new java.util.HashSet<>();
        for (VariableDecl param : decl.getParams()) {
            shadowed.add(param.getLabelLexeme());
        }
        for (VariableDecl local : decl.getLocalVars()) {
            shadowed.add(local.getLabelLexeme());
        }

        for (String label : callerMap.keySet()) {
            if (shadowed.contains(label)) {
                continue;
            }
            if (calleeMap.containsKey(label)) {
                callerMap.put(label, calleeMap.get(label));
            }
        }
    }


    @Override
    public String toString() {
        return name.getLexeme() + args.toString();
    }
}
