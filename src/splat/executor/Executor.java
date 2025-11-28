package splat.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import splat.parser.elements.Declaration;
import splat.parser.elements.FunctionCall;
import splat.parser.elements.FunctionDecl;
import splat.parser.elements.ProgramAST;
import splat.parser.elements.Statement;
import splat.parser.elements.VariableDecl;
import splat.semanticanalyzer.Type;

public class Executor {

    private ProgramAST progAST;

    private Map<String, FunctionDecl> funcMap;
    private Map<String, Value> progVarMap;

    public Executor(ProgramAST progAST) {
        this.progAST = progAST;
    }

    public void runProgram() throws ExecutionException {

        setMaps();

        try {
            for (Statement stmt : progAST.getStmts()) {
                stmt.execute(funcMap, progVarMap);
            }
        } catch (ReturnFromCall rfc) {
            throw new ExecutionException("Return cannot be used outside of a function", 0, 0);
        }
    }

    private void setMaps() {

        funcMap = new HashMap<>();
        progVarMap = new HashMap<>();

        for (Declaration decl : progAST.getDecls()) {

            String label = decl.getLabel().toString();

            if (decl instanceof FunctionDecl) {
                FunctionDecl funcDecl = (FunctionDecl)decl;
                funcMap.put(label, funcDecl);

            } else if (decl instanceof VariableDecl) {
                VariableDecl varDecl = (VariableDecl)decl;
                Type type = Type.fromToken(varDecl.getType());
                progVarMap.put(label, defaultValue(type));
            }
        }
    }

    public Value callFunction(FunctionCall call, Map<String, Value> callerVarMap)
            throws ExecutionException, ReturnFromCall {

        FunctionDecl decl = funcMap.get(call.getName().getLexeme());
        if (decl == null) {
            throw new ExecutionException("Unknown function '" + call.getName().getLexeme() + "'",
                    call.getLine(), call.getColumn());
        }

        List<VariableDecl> params = decl.getParams();
        if (params.size() != call.getArgs().size()) {
            throw new ExecutionException("Incorrect number of arguments for function", call.getLine(), call.getColumn());
        }

        Map<String, Value> newVarMap = new HashMap<>();
        // Include program-level variables for read/write access
        newVarMap.putAll(progVarMap);

        for (int i = 0; i < params.size(); i++) {
            VariableDecl param = params.get(i);
            Value argVal = call.getArgs().get(i).evaluate(funcMap, callerVarMap);
            newVarMap.put(param.getLabel().toString(), argVal);
        }

        for (VariableDecl local : decl.getLocalVars()) {
            Type type = Type.fromToken(local.getType());
            newVarMap.put(local.getLabel().toString(), defaultValue(type));
        }

        try {
            for (Statement stmt : decl.getStmts()) {
                stmt.execute(funcMap, newVarMap);
            }
        } catch (ReturnFromCall rfc) {
            return rfc.getValue();
        }

        return defaultValue(Type.fromToken(decl.getReturnType()));
    }

    private Value defaultValue(Type type) {
        switch (type) {
            case INTEGER:
                return Value.ofInteger(0);
            case BOOLEAN:
                return Value.ofBoolean(false);
            case STRING:
                return Value.ofString("");
            case VOID:
            default:
                return Value.voidValue();
        }
    }
}

