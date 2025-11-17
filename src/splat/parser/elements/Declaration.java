package splat.parser.elements;

import splat.lexer.Token;

public abstract class Declaration extends ASTElement {

    public Declaration(Token tok) {
        super(tok);
    }
}
