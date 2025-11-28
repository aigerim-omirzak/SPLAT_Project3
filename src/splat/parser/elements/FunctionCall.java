package splat.parser.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;
import splat.parser.elements.Statement;

public class FunctionCall extends Expression {
    private final Token name;
    private final List<Expression> args;

    public FunctionCall(Token name, List<Expression> args) {
        super(name);
        this.name = name;
        this.args = args;
    }

    public List<Expression> getArgs() { return args; }

    public Token getStartToken() {
        return name;
    }


    @Override
    public String toString() {
        return name.getLexeme() + args.toString();
    }

    @Override
    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                  Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type returnType = analyzeCall(funcMap, varAndParamMap);
        if (returnType == Type.VOID) {
            throw new SemanticAnalysisException(
                    "Void function '" + name.getLexeme() + "' cannot be used in an expression",
                    name.getLine(), name.getCol());
        }
        return returnType;
    }

    public Type analyzeCall(Map<String, FunctionDecl> funcMap,
                            Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        FunctionDecl decl = requireFunctionDeclaration(funcMap);
        verifyArgumentCount(decl);
        validateArgumentTypes(funcMap, varAndParamMap, decl);

        Token returnToken = decl.getReturnType();
        return Type.fromToken(returnToken);
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap,
                          Map<String, Value> varAndParamMap) throws ExecutionException {
        FunctionDecl declaration = fetchFunctionDeclaration(funcMap);
        Map<String, Value> callContext = prepareCallContext(declaration, funcMap, varAndParamMap);

        List<Statement> body = declaration.getBody();
        try {
            if (body != null) {
                for (Statement stmt : body) {
                    stmt.execute(funcMap, callContext);
                }
            }
        } catch (ReturnFromCall ret) {
            return ret.getReturnVal();
        }

        ensureVoidReturn(declaration);
        return null;
    }

    private FunctionDecl requireFunctionDeclaration(Map<String, FunctionDecl> funcMap) throws SemanticAnalysisException {
        String funcName = name.getLexeme();
        FunctionDecl decl = funcMap.get(funcName);
        if (decl == null) {
            throw new SemanticAnalysisException(
                    "Function '" + funcName + "' is not defined",
                    name.getLine(), name.getCol());
        }
        return decl;
    }

    private void verifyArgumentCount(FunctionDecl decl) throws SemanticAnalysisException {
        List<VariableDecl> params = decl.getParams();
        if (params.size() != args.size()) {
            throw new SemanticAnalysisException(
                    "Function '" + name.getLexeme() + "' expects " + params.size()
                            + " arguments but got " + args.size(),
                    name.getLine(), name.getCol());
        }
    }

    private void validateArgumentTypes(Map<String, FunctionDecl> funcMap,
                                       Map<String, Type> varAndParamMap,
                                       FunctionDecl decl) throws SemanticAnalysisException {
        List<VariableDecl> params = decl.getParams();
        for (int i = 0; i < params.size(); i++) {
            VariableDecl paramDecl = params.get(i);
            Type expected = Type.fromToken(paramDecl.getType());
            if (expected == Type.VOID) {
                throw new SemanticAnalysisException(
                        "Parameter '" + paramDecl.getName().getLexeme() + "' cannot be void",
                        paramDecl.getLine(), paramDecl.getColumn());
            }

            Type actual = args.get(i).analyzeAndGetType(funcMap, varAndParamMap);
            if (expected != actual) {
                throw new SemanticAnalysisException(
                        "Argument " + (i + 1) + " for function '" + name.getLexeme()
                                + "' expected type " + expected + " but found " + actual,
                        args.get(i).getLine(), args.get(i).getColumn());
            }
        }
    }

    private FunctionDecl fetchFunctionDeclaration(Map<String, FunctionDecl> funcMap) throws ExecutionException {
        FunctionDecl decl = funcMap.get(name.getLexeme());
        if (decl == null) {
            throw new ExecutionException(
                    "Function '" + name.getLexeme() + "' is not defined",
                    name.getLine(), name.getCol());
        }
        return decl;
    }

    private Map<String, Value> prepareCallContext(FunctionDecl decl,
                                                 Map<String, FunctionDecl> funcMap,
                                                 Map<String, Value> varAndParamMap) throws ExecutionException {
        List<Value> argVals = new ArrayList<>();
        for (Expression arg : args) {
            argVals.add(arg.evaluate(funcMap, varAndParamMap));
        }

        Map<String, Value> callVarMap = new HashMap<>();
        List<VariableDecl> params = decl.getParams();
        for (int i = 0; i < params.size(); i++) {
            VariableDecl paramDecl = params.get(i);
            callVarMap.put(paramDecl.getName().getLexeme(), argVals.get(i));
        }

        if (decl.getLocalVars() != null) {
            for (VariableDecl local : decl.getLocalVars()) {
                try {
                    Type type = Type.fromToken(local.getType());
                    callVarMap.put(local.getName().getLexeme(), Value.defaultValue(type));
                } catch (SemanticAnalysisException ex) {
                    throw new ExecutionException(ex.getMessage(), local.getLine(), local.getColumn());
                }
            }
        }
        return callVarMap;
    }

    private void ensureVoidReturn(FunctionDecl decl) throws ExecutionException {
        try {
            Type returnType = Type.fromToken(decl.getReturnType());
            if (returnType != Type.VOID) {
                throw new ExecutionException("Function did not return a value", name.getLine(), name.getCol());
            }
        } catch (SemanticAnalysisException ex) {
            throw new ExecutionException(ex.getMessage(), name.getLine(), name.getCol());
        }
    }
}
