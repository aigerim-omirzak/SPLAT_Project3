package splat.parser.elements;

import java.util.Map;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.semanticanalyzer.Type;

public abstract class Statement extends ASTElement {

        public Statement(Token tok) {
                super(tok);
        }

        public abstract void analyze(Map<String, FunctionDecl> funcMap,
                                     Map<String, Type> varAndParamMap) throws SemanticAnalysisException;

        public abstract void execute(Map<String, FunctionDecl> funcMap,
                                     Map<String, Value> varAndParamMap)
                                     throws ReturnFromCall, ExecutionException;
}
