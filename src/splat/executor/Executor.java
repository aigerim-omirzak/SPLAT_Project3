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

public class Executor {

        private ProgramAST progAST;

        private Map<String, FunctionDecl> funcMap;
        private Map<String, Value> progVarMap;

        public Executor(ProgramAST progAST) {
                this.progAST = progAST;
        }

        public void runProgram() throws ExecutionException {

                // This sets the maps that will be needed for executing function
                // calls and storing the values of the program variables
                setMaps();

                try {

                        // Go through and execute each of the statements
                        for (Statement stmt : progAST.getStmts()) {
                                stmt.execute(funcMap, progVarMap);
                        }

                // We should never have to catch this exception here, since the
                // main program body cannot have returns
                } catch (ReturnFromCall ex) {
                        System.out.println("Internal error!!! The main program body "
                                        + "cannot have a return statement -- this should have "
                                        + "been caught during semantic analysis!");

                        throw new ExecutionException("Internal error -- fix your "
                                        + "semantic analyzer!", -1, -1);
                }
        }

        private void setMaps() throws ExecutionException {
                funcMap = new HashMap<>();
                progVarMap = new HashMap<>();

                for (Declaration decl : progAST.getDecls()) {
                        if (decl instanceof FunctionDecl) {
                                FunctionDecl funcDecl = (FunctionDecl) decl;
                                funcMap.put(funcDecl.getName().getLexeme(), funcDecl);
                        } else if (decl instanceof VariableDecl) {
                                VariableDecl varDecl = (VariableDecl) decl;
                                try {
                                        Type type = Type.fromToken(varDecl.getType());
                                        progVarMap.put(varDecl.getName().getLexeme(), Value.defaultValue(type));
                                } catch (SemanticAnalysisException ex) {
                                        throw new ExecutionException(ex.getMessage(), varDecl.getLine(), varDecl.getColumn());
                                }
                        }
                }
        }

}
