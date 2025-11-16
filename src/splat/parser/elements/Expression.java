package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public abstract class Expression extends ASTElement {

    public Expression(Token tok) {
        super(tok);
    }

    /**
     * Performs semantic analysis on this expression and returns its type.
     */
    public abstract Type analyzeAndGetType(Map<String, FunctionDecl> funcMap,
                                           Map<String, Type> varAndParamMap)
            throws SemanticAnalysisException;

    /**
     * This will be needed for Phase 4 - this abstract method will need to be
     * implemented by every Expression subclass.  This method is used to
     * "calculate" the value of this expression, which will usually require we
     * recursively call the same method on all sub-expressions.
     */
//      public abstract Value evaluate(Map<String, FunctionDecl> funcMap,
//                                 Map<String, Value> varAndParamMap);
}
