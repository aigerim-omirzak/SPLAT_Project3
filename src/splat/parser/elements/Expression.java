package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public abstract class Expression extends ASTElement {

    public Expression(Token tok) {
        super(tok);
    }

    public abstract Type analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                           Map<String, Type> varAndParamMap)
            throws SemanticAnalysisException;

    /**
     * This will be needed for Phase 4 - this abstract method will need to be
     * implemented by every Expression subclass.  This method is used to
     * "calculate" the value of this expression, which will usually require we
     * recursively call the same method on all sub-expressions.
     *
     * funcMap is needed in case this expression or a sub-expression contains
     * a function call -- we will have to evaluate the individual arguments and
     * create a new varAndParamMap to bind the function params to the new values
     * and then execute the function body.  More on this later...
     *
     * varAndParamMap is needed in case this expression or a sub-expression
     * contains variables or parameters -- we use this map to keep track of the
     * values of the items that are currently in scope
     */
     public abstract Value evaluate(Map<String, FunctionDecl> funcMap,
                                 Map<String, Value> varAndParamMap) throws ExecutionException;
}
