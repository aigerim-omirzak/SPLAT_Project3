package splat.executor;

import java.util.HashMap;
import java.util.Map;

import splat.parser.elements.Declaration;
import splat.parser.elements.FunctionDecl;
import splat.parser.elements.ProgramAST;
import splat.parser.elements.Statement;
import splat.parser.elements.VariableDecl;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;
import splat.executor.ReturnFromCall;

public class Executor {

    private final ProgramAST progAST;

    private Map<String, FunctionDecl> funcMap;
    private Map<String, Value> progVarMap;

    public Executor(ProgramAST progAST) {
        this.progAST = progAST;
    }

    public void runProgram() throws ExecutionException {
        initializeMaps();

        try {
            for (Statement stmt : progAST.getStmts()) {
                stmt.execute(funcMap, progVarMap);
            }

        } catch (ReturnFromCall ex) {
            System.out.println("Internal error!!! The main program body "
                    + "cannot have a return statement -- this should have "
                    + "been caught during semantic analysis!");

            throw new ExecutionException("Internal error -- fix your "
                    + "semantic analyzer!", -1, -1);
        }
    }

    private void initializeMaps() throws ExecutionException {
        funcMap = new HashMap<>();
        progVarMap = new HashMap<>();

        for (Declaration decl : progAST.getDecls()) {
            if (decl instanceof FunctionDecl) {
                FunctionDecl funcDecl = (FunctionDecl) decl;
                funcMap.put(funcDecl.getName().getLexeme(), funcDecl);
            } else if (decl instanceof VariableDecl) {
                registerVariable((VariableDecl) decl);
            }
        }
    }

    private void registerVariable(VariableDecl varDecl) throws ExecutionException {
        try {
            Type type = Type.fromToken(varDecl.getType());
            progVarMap.put(varDecl.getName().getLexeme(), Value.defaultValue(type));
        } catch (SemanticAnalysisException ex) {
            throw new ExecutionException(ex.getMessage(), varDecl.getLine(), varDecl.getColumn());
        }
    }

}
