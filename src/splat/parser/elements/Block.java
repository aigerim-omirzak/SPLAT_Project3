package splat.parser.elements;

import java.util.List;
import java.util.Map;

import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

public class Block extends Statement {
    private List<Statement> statements;

    public Block(Token tok, List<Statement> stmts) {
        super(tok);
        this.statements = stmts;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public Token getStartToken() {
        return super.getStartToken();
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap,
                        Map<String, String> varAndParamMap) throws SemanticAnalysisException {
        for (Statement stmt : statements) {
            stmt.analyze(funcMap, varAndParamMap);
        }
    }
}
