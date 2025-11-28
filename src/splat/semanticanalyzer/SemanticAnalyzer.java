package splat.semanticanalyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import splat.parser.elements.Declaration;
import splat.parser.elements.FunctionDecl;
import splat.parser.elements.ProgramAST;
import splat.parser.elements.Statement;
import splat.semanticanalyzer.Type;
import splat.parser.elements.VariableDecl;

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
			stmt.analyze(funcMap, progVarMap);
		}
		
	}

        private void analyzeFuncDecl(FunctionDecl funcDecl) throws SemanticAnalysisException {
		
		// Checks to make sure we don't use the same labels more than once
		// among our function parameters, local variables, and function names
		checkNoDuplicateFuncLabels(funcDecl);
		
		// Get the types of the parameters and local variables
                Map<String, Type> varAndParamMap = getVarAndParamMap(funcDecl);
		
		// Perform semantic analysis on the function body
		for (Statement stmt : funcDecl.getStmts()) {
			stmt.analyze(funcMap, varAndParamMap);
		}
	}
	
	
        private Map<String, Type> getVarAndParamMap(FunctionDecl funcDecl) throws SemanticAnalysisException {

                Map<String, Type> map = new HashMap<>();
                for (VariableDecl param : funcDecl.getParams()) {
                        map.put(param.getLabelLexeme(), Type.fromToken(param.getType()));
                }
                for (VariableDecl local : funcDecl.getLocalVars()) {
                        map.put(local.getLabelLexeme(), Type.fromToken(local.getType()));
                }
                // include program variables for reference within functions
                map.putAll(progVarMap);
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
