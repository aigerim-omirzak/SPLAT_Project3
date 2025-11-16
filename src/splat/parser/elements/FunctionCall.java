package splat.parser.elements;

import java.util.List;
import splat.lexer.Token;

public class FunctionCall extends Expression {
    private final Token name;
    private final List<Expression> args;

    public FunctionCall(Token name, List<Expression> args) {
        super(name);
        this.name = name;
        this.args = args;
    }

    public Token getName() { return name; }
    public List<Expression> getArgs() { return args; }

    public Token getStartToken() {
        return name;
    }


    @Override
    public String toString() {
        return name.getLexeme() + args.toString();
    }
}
