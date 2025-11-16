package splat.semanticanalyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    private final ProgramAST program;
    private final Map<String, FunctionDecl> functionMap;
    private final Map<String, Type> globalVarMap;

    public SemanticAnalyzer(ProgramAST program) {
        this.program = program;
        this.functionMap = new HashMap<>();
        this.globalVarMap = new HashMap<>();
    }

    public void analyze() throws SemanticAnalysisException {
        collectGlobalDeclarations();
        analyzeFunctions();
        analyzeProgramBody();
    }

    private void collectGlobalDeclarations() throws SemanticAnalysisException {
        Set<String> labels = new HashSet<>();
        for (Declaration decl : program.getDecls()) {
            String label = decl.getLabelLexeme();
            if (!labels.add(label)) {
                throw new SemanticAnalysisException(
                        "Duplicate declaration: '" + label + "'",
                        decl.getLine(), decl.getColumn());
            }

            if (decl instanceof VariableDecl) {
                VariableDecl varDecl = (VariableDecl) decl;
                Type type = Type.fromToken(varDecl.getType());
                if (type == Type.VOID) {
                    throw new SemanticAnalysisException(
                            "Variables cannot be declared with type void",
                            varDecl.getLine(), varDecl.getColumn());
                }
                globalVarMap.put(label, type);
            } else if (decl instanceof FunctionDecl) {
                functionMap.put(label, (FunctionDecl) decl);
            }
        }
    }

    private void analyzeFunctions() throws SemanticAnalysisException {
        for (FunctionDecl functionDecl : functionMap.values()) {
            analyzeFunction(functionDecl);
        }
    }

    private void analyzeFunction(FunctionDecl functionDecl) throws SemanticAnalysisException {
        Map<String, Type> scope = new HashMap<>();
        Set<String> localNames = new HashSet<>();

        // Parameters
        List<VariableDecl> params = functionDecl.getParams();
        if (params != null) {
            for (VariableDecl param : params) {
                registerLocalName(param, localNames);
                ensureNotFunctionName(param.getName().getLexeme(), param.getLine(), param.getColumn());
                Type type = Type.fromToken(param.getType());
                if (type == Type.VOID) {
                    throw new SemanticAnalysisException(
                            "Parameters cannot be declared with type void",
                            param.getLine(), param.getColumn());
                }
                scope.put(param.getName().getLexeme(), type);
            }
        }

        // Local variables
        List<VariableDecl> locals = functionDecl.getLocalVars();
        if (locals != null) {
            for (VariableDecl localVar : locals) {
                registerLocalName(localVar, localNames);
                ensureNotFunctionName(localVar.getName().getLexeme(), localVar.getLine(), localVar.getColumn());
                Type type = Type.fromToken(localVar.getType());
                if (type == Type.VOID) {
                    throw new SemanticAnalysisException(
                            "Local variables cannot be declared with type void",
                            localVar.getLine(), localVar.getColumn());
                }
                scope.put(localVar.getName().getLexeme(), type);
            }
        }

        Type returnType = Type.fromToken(functionDecl.getReturnType());
        scope.put(Statement.RETURN_TYPE_SLOT, returnType);

        List<Statement> body = functionDecl.getBody();
        if (body != null) {
            for (Statement stmt : body) {
                stmt.analyze(functionMap, scope);
            }
        }

        if (returnType != Type.VOID && !containsReturn(body)) {
            throw new SemanticAnalysisException(
                    "Function '" + functionDecl.getName().getLexeme() + "' must return a value",
                    functionDecl.getLine(), functionDecl.getColumn());
        }
    }

    private void analyzeProgramBody() throws SemanticAnalysisException {
        Map<String, Type> scope = new HashMap<>(globalVarMap);
        for (Statement stmt : program.getStmts()) {
            stmt.analyze(functionMap, scope);
        }
    }

    private void registerLocalName(VariableDecl decl, Set<String> names) throws SemanticAnalysisException {
        String label = decl.getName().getLexeme();
        if (!names.add(label)) {
            throw new SemanticAnalysisException(
                    "Duplicate declaration inside function: '" + label + "'",
                    decl.getLine(), decl.getColumn());
        }
    }

    private void ensureNotFunctionName(String name, int line, int column) throws SemanticAnalysisException {
        if (functionMap.containsKey(name)) {
            throw new SemanticAnalysisException(
                    "Identifier '" + name + "' conflicts with an existing function name",
                    line, column);
        }
    }

    private boolean containsReturn(List<Statement> statements) {
        if (statements == null) {
            return false;
        }

        for (Statement stmt : statements) {
            if (stmt instanceof ReturnStmt) {
                return true;
            }

            if (stmt instanceof IfThenElse) {
                IfThenElse ite = (IfThenElse) stmt;
                if (containsReturn(ite.getThenStmts()) || containsReturn(ite.getElseStmts())) {
                    return true;
                }
            } else if (stmt instanceof WhileLoop) {
                if (containsReturn(((WhileLoop) stmt).getBody())) {
                    return true;
                }
            } else if (stmt instanceof Block) {
                if (containsReturn(((Block) stmt).getStatements())) {
                    return true;
                }
            }
        }

        return false;
    }
}
