package splat.semanticanalyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import splat.parser.elements.Block;
import splat.parser.elements.Declaration;
import splat.parser.elements.FunctionDecl;
import splat.parser.elements.IfThenElse;
import splat.parser.elements.ProgramAST;
import splat.parser.elements.ReturnStmt;
import splat.parser.elements.Statement;
import splat.parser.elements.VariableDecl;
import splat.parser.elements.WhileLoop;

public class SemanticAnalyzer {

        private ProgramAST progAST;

        private Map<String, FunctionDecl> funcMap;
        private Map<String, Type> progVarMap;
	
	public SemanticAnalyzer(ProgramAST progAST) {
		this.progAST = progAST;
	}

        public void analyze() throws SemanticAnalysisException {

                // Checks to make sure we don't use the same labels more than once
                // for our program functions and variables
                checkNoDuplicateProgLabels();
		
                // This sets the maps that will be needed later when we need to
                // typecheck variable references and function calls in the
                // program body
                setProgVarAndFuncMaps();
		
		// Perform semantic analysis on the functions
		for (FunctionDecl funcDecl : funcMap.values()) {	
			analyzeFuncDecl(funcDecl);
        }
		
                // Perform semantic analysis on the program body
                for (Statement stmt : progAST.getStmts()) {
                        if (containsReturn(stmt)) {
                                throw new SemanticAnalysisException("Return cannot be used outside of a function",
                                                stmt.getLine(), stmt.getColumn());
                        }
                        stmt.analyze(funcMap, progVarMap);
                }

        }

        private void analyzeFuncDecl(FunctionDecl funcDecl) throws SemanticAnalysisException {

                // Checks to make sure we don't use the same labels more than once
                // among our function parameters, local variables, and function names
                checkNoDuplicateFuncLabels(funcDecl);

                for (VariableDecl param : funcDecl.getParams()) {
                        String label = param.getLabelLexeme();
                        if (funcMap.containsKey(label) && funcMap.get(label) != funcDecl) {
                                throw new SemanticAnalysisException("Duplicate label '" + label + "' in function",
                                                param.getLine(), param.getColumn());
                        }
                }

                // Get the types of the parameters and local variables
                Map<String, Type> varAndParamMap = getVarAndParamMap(funcDecl);

                // Perform semantic analysis on the function body
                for (Statement stmt : funcDecl.getStmts()) {
                        stmt.analyze(funcMap, varAndParamMap);
                }

                // Ensure non-void functions eventually return a value
                Type returnType = Type.fromToken(funcDecl.getReturnType());
                if (returnType != Type.VOID && !containsReturn(funcDecl.getStmts())) {
                        throw new SemanticAnalysisException("Function is missing a return statement",
                                        funcDecl.getLine(), funcDecl.getColumn());
                }

                validateReturnTypes(funcDecl.getStmts(), varAndParamMap, returnType, funcDecl);
        }

        private void validateReturnTypes(Iterable<Statement> statements, Map<String, Type> varAndParamMap,
                                         Type expectedReturn, FunctionDecl funcDecl) throws SemanticAnalysisException {
                for (Statement stmt : statements) {
                        validateReturnType(stmt, varAndParamMap, expectedReturn, funcDecl);
                }
        }

        private void validateReturnType(Statement stmt, Map<String, Type> varAndParamMap,
                                        Type expectedReturn, FunctionDecl funcDecl) throws SemanticAnalysisException {
                if (stmt instanceof ReturnStmt) {
                        ReturnStmt ret = (ReturnStmt) stmt;
                        if (expectedReturn == Type.VOID) {
                                if (ret.getExpr() != null) {
                                        throw new SemanticAnalysisException("Void functions cannot return a value",
                                                        ret.getReturnToken().getLine(), ret.getReturnToken().getCol());
                                }
                                return;
                        }

                        if (ret.getExpr() == null) {
                                throw new SemanticAnalysisException("Return statement must return a value",
                                                ret.getReturnToken().getLine(), ret.getReturnToken().getCol());
                        }

                        Type actual = ret.getExpr().analyzeAndGetType(funcMap, varAndParamMap);
                        if (actual != expectedReturn) {
                                throw new SemanticAnalysisException("Return type mismatch", ret);
                        }
                        return;
                }

                if (stmt instanceof Block) {
                        validateReturnTypes(((Block) stmt).getStatements(), varAndParamMap, expectedReturn, funcDecl);
                } else if (stmt instanceof IfThenElse) {
                        IfThenElse ite = (IfThenElse) stmt;
                        validateReturnTypes(ite.getThenStmts(), varAndParamMap, expectedReturn, funcDecl);
                        validateReturnTypes(ite.getElseStmts(), varAndParamMap, expectedReturn, funcDecl);
                } else if (stmt instanceof WhileLoop) {
                        validateReturnTypes(((WhileLoop) stmt).getBody(), varAndParamMap, expectedReturn, funcDecl);
                }
        }

        private boolean containsReturn(Statement stmt) {
                if (stmt instanceof ReturnStmt) {
                        return true;
                }

                if (stmt instanceof Block) {
                        return containsReturn(((Block) stmt).getStatements());
                }

                if (stmt instanceof IfThenElse) {
                        IfThenElse ite = (IfThenElse) stmt;
                        return containsReturn(ite.getThenStmts()) || containsReturn(ite.getElseStmts());
                }

                if (stmt instanceof WhileLoop) {
                        return containsReturn(((WhileLoop) stmt).getBody());
                }

                return false;
        }

        private boolean containsReturn(Iterable<Statement> statements) {
                for (Statement stmt : statements) {
                        if (containsReturn(stmt)) {
                                return true;
                        }
                }
                return false;
        }
	
	
        private Map<String, Type> getVarAndParamMap(FunctionDecl funcDecl) throws SemanticAnalysisException {

                Map<String, Type> map = new HashMap<>();
                for (VariableDecl param : funcDecl.getParams()) {
                        map.put(param.getLabelLexeme(), Type.fromToken(param.getType()));
                }
                for (VariableDecl local : funcDecl.getLocalVars()) {
                        map.put(local.getLabelLexeme(), Type.fromToken(local.getType()));
                }
                return map;
        }

        private void checkNoDuplicateFuncLabels(FunctionDecl funcDecl)
                                                                        throws SemanticAnalysisException {

                Set<String> labels = new HashSet<>();
                labels.add(funcDecl.getLabelLexeme());

                for (VariableDecl param : funcDecl.getParams()) {
                        String label = param.getLabelLexeme();
                        if (labels.contains(label)) {
                                throw new SemanticAnalysisException("Duplicate label '" + label + "' in function",
                                                param.getLine(), param.getColumn());
                        }
                        labels.add(label);
                }

                for (VariableDecl local : funcDecl.getLocalVars()) {
                        String label = local.getLabelLexeme();
                        if (labels.contains(label)) {
                                throw new SemanticAnalysisException("Duplicate label '" + label + "' in function",
                                                local.getLine(), local.getColumn());
                        }
                        labels.add(label);
                }
        }
	
        private void checkNoDuplicateProgLabels() throws SemanticAnalysisException {

                Set<String> labels = new HashSet<String>();

                for (Declaration decl : progAST.getDecls()) {
                        String label = decl.getLabelLexeme();
 			
			if (labels.contains(label)) {
				throw new SemanticAnalysisException("Cannot have duplicate label '"
						+ label + "' in program", decl);
			} else {
				labels.add(label);
			}
			
		}
	}
	
        private void setProgVarAndFuncMaps() throws SemanticAnalysisException {

                funcMap = new HashMap<>();
                progVarMap = new HashMap<>();

                for (Declaration decl : progAST.getDecls()) {

                        String label = decl.getLabelLexeme();
			
			if (decl instanceof FunctionDecl) {
				FunctionDecl funcDecl = (FunctionDecl)decl;
				funcMap.put(label, funcDecl);
				
			} else if (decl instanceof VariableDecl) {
				VariableDecl varDecl = (VariableDecl)decl;
                                progVarMap.put(label, Type.fromToken(varDecl.getType()));
                        }
                }
        }
}
