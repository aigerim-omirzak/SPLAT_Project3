package splat.parser.elements;

import java.util.List;

import splat.lexer.Token;

public class ProgramAST extends ASTElement {

    private final List<Declaration> decls;
    private final List<Statement> stmts;

    public ProgramAST(List<Declaration> decls,
                      List<Statement> stmts,
                      Token tok) {

        super(tok);
        this.decls = decls;
        this.stmts = stmts;
    }

    public List<Declaration> getDecls() {
        return decls;
    }

    public List<Statement> getStmts() {
        return stmts;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("program \n");
        for (Declaration decl : decls) {
            result.append("   ").append(decl).append("\n");
        }
        result.append("begin \n");
        for (Statement stmt : stmts) {
            result.append("   ").append(stmt).append("\n");
        }
        result.append("end;");

        return result.toString();
    }
}
