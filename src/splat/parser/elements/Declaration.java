package splat.parser.elements;

import splat.lexer.Token;


public abstract class Declaration extends ASTElement {

    private final Token labelToken;

    public Declaration(Token tok) {
        super(tok);
        this.labelToken = tok;
    }

    public Token getLabel() {
        return labelToken;
    }

    public String getLabelLexeme() {
        return labelToken.getLexeme();
    }
}
