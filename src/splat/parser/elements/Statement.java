package splat.parser.elements;

import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public abstract class Statement extends ASTElement {

    public static final String RETURN_TYPE_SLOT = "0result";

    public Statement(Token tok) {
        super(tok);
    }

    /**
     * Возвращает токен, с которого начинается statement.
     * Все подклассы могут переопределять этот метод.
     */
    public Token getStartToken() {
        return super.getToken();
    }

    /**
     * Performs semantic analysis on the statement and its children.
     */
    public abstract void analyze(Map<String, FunctionDecl> funcMap,
                                 Map<String, Type> varAndParamMap)
            throws SemanticAnalysisException;

    /**
     * This will be needed for Phase 4 - this abstract method will need to be
     * implemented by every Statement subclass.  This method is used to
     * execute each statement, which may result in output to the console, or
     * updating the varAndParamMap.  Both of the given maps may be needed for
     * evaluating any sub-expressions in the statement.
     */
//       public abstract void execute(Map<String, FunctionDecl> funcMap,
//                                    Map<String, Value> varAndParamMap)
//                                                                              throws ReturnFromCall;
}
