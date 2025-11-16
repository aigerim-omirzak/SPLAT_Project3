package splat.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import splat.lexer.Token;
import splat.parser.elements.*;

public class Parser {

    private List<Token> tokens;
    private boolean hasErrors = false;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private void debug(String msg) {
        System.err.println("[DEBUG] " + msg);
    }

    /*-----------------------------------------
     * Utility methods
     *----------------------------------------*/

    private Token nextToken() throws ParseException {
        if (tokens.isEmpty())
            throw new ParseException("Unexpected end of input.", null);
        return tokens.remove(0);
    }

    private Token expect(String expected) throws ParseException {
        if (tokens.isEmpty())
            throw new ParseException("Unexpected end of input, expected '" + expected + "'", null);
        Token t = tokens.get(0);
        if (!t.getLexeme().equals(expected)) {
            throw new ParseException("Expected '" + expected + "', got '" + t.getLexeme() + "'.", t);
        }
        return tokens.remove(0);
    }

    private boolean peek(String expected) {
        return !tokens.isEmpty() && tokens.get(0).getLexeme().equals(expected);
    }

    private boolean match(String expected) throws ParseException {
        if (peek(expected)) {
            nextToken();
            return true;
        }
        return false;
    }

    private void skipStrayTokens() {
        while (!tokens.isEmpty() && (peek(";") || peek(":"))) {
            tokens.remove(0);
        }
    }

    private static final Set<String> KEYWORDS = new HashSet<>();
    static {
        String[] keys = { "if", "then", "else", "while", "loop", "do",
                "begin", "end", "program", "return", "is",
                "print", "print_line", "not" };
        for (String k : keys) KEYWORDS.add(k);
    }

    private Token checkIdentifier() throws ParseException {
        if (tokens.isEmpty())
            throw new ParseException("Unexpected end of input, expected identifier.", null);
        Token tok = tokens.get(0);
        if (!isIdentifier(tok.getLexeme())) {
            throw new ParseException("Expected identifier, got '" + tok.getLexeme() + "'", tok);
        }
        return tokens.remove(0);
    }

    private boolean isIdentifier(String val) {
        return val != null && !val.isEmpty() && !KEYWORDS.contains(val) && val.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private void skipUntil(String... stopTokens) {
        while (!tokens.isEmpty()) {
            String current = tokens.get(0).getLexeme();
            for (String stop : stopTokens) {
                if (current.equals(stop)) {
                    return;
                }
            }
            tokens.remove(0);
        }
    }

    /*-----------------------------------------
     * Program
     *----------------------------------------*/
    public ProgramAST parse() throws ParseException {
        if (!tokens.isEmpty()) {
            String firstLexeme = tokens.get(0).getLexeme();
            // Логируем первые 10 токенов для диагностики
            System.err.println("DEBUG PARSER START - First 10 tokens: " +
                    tokens.subList(0, Math.min(10, tokens.size())));
        }

        try {
            expect("program");

            // Program name
            Token progName;
            if (!tokens.isEmpty() && isIdentifier(tokens.get(0).getLexeme())) {
                progName = checkIdentifier();
            } else {
                progName = new Token("main", tokens.isEmpty() ? 0 : tokens.get(0).getLine(),
                        tokens.isEmpty() ? 0 : tokens.get(0).getCol());
            }

            skipStrayTokens();

            List<Declaration> decls = parseDecls();
            expect("begin");
            List<Statement> stmts = parseStmts();
            expect("end");

            if (peek(";")) {
                try {
                    nextToken();
                } catch (ParseException e) {
                    // ignore
                }
            }

            return new ProgramAST(decls, stmts, progName);
        } catch (ParseException e) {
            hasErrors = true;
            throw e;
        }
    }

    /*-----------------------------------------
     * Declarations
     *----------------------------------------*/
    private List<Declaration> parseDecls() {
        List<Declaration> decls = new ArrayList<>();
        Set<String> declaredNames = new HashSet<>();

        while (!tokens.isEmpty() && !peek("begin")) {
            skipStrayTokens();
            if (tokens.isEmpty() || peek("begin")) break;

            if (isIdentifier(tokens.get(0).getLexeme())) {
                try {
                    String potentialName = tokens.get(0).getLexeme();
                    if (declaredNames.contains(potentialName)) {
                        throw new ParseException("Duplicate declaration: '" + potentialName + "'", tokens.get(0));
                    }

                    Declaration decl = parseDecl();
                    if (decl != null) {
                        decls.add(decl);
                        declaredNames.add(potentialName);
                    }
                } catch (ParseException e) {
                    skipUntil("begin", "end", ";");
                }
            } else {
                if (!peek("begin")) {
                    tokens.remove(0);
                }
            }
        }

        return decls;
    }

    private Declaration parseDecl() throws ParseException {
        System.err.println("DEBUG parseDecl: tokens = " + tokens.subList(0, Math.min(5, tokens.size())));

        Token nameTok = checkIdentifier();
        System.err.println("DEBUG: nameTok = " + nameTok.getLexeme());

        if (tokens.isEmpty()) {
            throw new ParseException("Unexpected end of input after identifier", nameTok);
        }

        Token next = tokens.get(0);
        System.err.println("DEBUG: next token = " + next.getLexeme());

        if (next.getLexeme().equals(":")) {
            System.err.println("DEBUG: parsing variable declaration");
            return parseVariableDecl();
        } else if (next.getLexeme().equals("is")) {
            System.err.println("DEBUG: parsing function without params");
            return parseFunctionDeclNoParams(nameTok);
        } else if (next.getLexeme().equals("(")) {
            System.err.println("DEBUG: parsing function with params");
            return parseFunctionDecl(nameTok);
        } else {
            System.err.println("DEBUG: throwing parse exception");
            throw new ParseException("Expected ':' or 'is' after identifier, got '" + next.getLexeme() + "'", next);
        }
    }


    private VariableDecl parseVariableDecl() throws ParseException {
        Token nameTok = checkIdentifier();
        expect(":");
        Token typeTok = checkIdentifier();

        if (peek(";")) {
            nextToken();
        }

        return new VariableDecl(nameTok, typeTok);
    }




    private FunctionDecl parseFunctionDecl(Token nameTok) throws ParseException {
        expect("(");
        List<VariableDecl> params = parseParamList();

        Token returnType = null;
        if (peek(":")) {
            nextToken();
            returnType = checkIdentifier();
        }

        expect("is");
        expect("begin");

        List<VariableDecl> localVars = new ArrayList<>();

        // НА Phase 2 НЕ ПРОВЕРЯЕМ ДУБЛИРОВАНИЕ С ПАРАМЕТРАМИ - это для Phase 3
        while (!tokens.isEmpty() && isIdentifier(tokens.get(0).getLexeme()) &&
                tokens.size() > 1 && tokens.get(1).getLexeme().equals(":")) {
            localVars.add(parseVariableDecl());
        }

        List<Statement> bodyStmts = parseStmts();

        // НА Phase 2 НЕ ПРОВЕРЯЕМ НАЛИЧИЕ RETURN - это для Phase 3
        expect("end");

        if (!tokens.isEmpty() && tokens.get(0).getLexeme().equals(nameTok.getLexeme())) {
            nextToken();
        }

        if (peek(";")) nextToken();

        return new FunctionDecl(nameTok, params, returnType, localVars, bodyStmts);
    }


    private FunctionDecl parseFunctionDeclNoParams(Token nameTok) throws ParseException {
        expect("is");
        expect("begin");

        List<VariableDecl> localVars = new ArrayList<>();
        while (!tokens.isEmpty() && isIdentifier(tokens.get(0).getLexeme()) &&
                tokens.size() > 1 && tokens.get(1).getLexeme().equals(":")) {
            localVars.add(parseVariableDecl());
        }

        List<Statement> bodyStmts = parseStmts();
        expect("end");

        // Опциональное имя функции после end
        if (!tokens.isEmpty() && tokens.get(0).getLexeme().equals(nameTok.getLexeme())) {
            nextToken();
        }

        if (peek(";")) nextToken();

        return new FunctionDecl(nameTok, new ArrayList<>(), null, localVars, bodyStmts);
    }

    private List<VariableDecl> parseParamList() throws ParseException {
        List<VariableDecl> params = new ArrayList<>();
        expect("(");

        if (!peek(")")) {
            do {
                Token paramName = checkIdentifier();
                expect(":");
                Token paramType = checkIdentifier();
                params.add(new VariableDecl(paramName, paramType));
            } while (match(","));
        }

        expect(")");
        return params;
    }

    /*-----------------------------------------
     * Statements - IMPROVED VERSION
     *----------------------------------------*/
    private List<Statement> parseStmts() throws ParseException {
        List<Statement> stmts = new ArrayList<>();

        while (!tokens.isEmpty()) {
            String current = tokens.get(0).getLexeme();

            if (current.equals("end") || current.equals("else")) {
                break;
            }

            if (current.equals(";")) {
                tokens.remove(0);
                continue;
            }

            // Проверка на объявления переменных в statements section
            if (isIdentifier(current) && tokens.size() > 1 && tokens.get(1).getLexeme().equals(":")) {
                throw new ParseException("Variable declarations are not allowed in statements section", tokens.get(0));
            }

            // Проверка на другие недопустимые конструкции
            if (current.equals(":")) {
                throw new ParseException("Unexpected ':' in statements section", tokens.get(0));
            }

            // Проверка на недопустимые ключевые слова в statements
            if (current.equals("program") || current.equals("is")) {
                throw new ParseException("Unexpected keyword in statements section: " + current, tokens.get(0));
            }

            Statement stmt = parseStmt();
            stmts.add(stmt);
        }

        return stmts;
    }

    // ======================
// parseStmt() для Phase 2
// ======================
    private Statement parseStmt() throws ParseException {
        if (tokens.isEmpty()) {
            throw new ParseException("Unexpected end of input in statement", null);
        }

        String first = tokens.get(0).getLexeme();

        switch (first) {
            case "if":
                return parseIf();
            case "while":
                return parseWhile();
            case "print":
            case "print_line":
                return parsePrint();
            case "return":
                return parseReturn();
            case "begin":
                return parseBlock();
            default:
                if (isIdentifier(first)) {
                    if (tokens.size() > 1 && tokens.get(1).getLexeme().equals(":=")) {
                        return parseAssign();
                    } else if (tokens.size() > 1 && tokens.get(1).getLexeme().equals("(")) {
                        Token funcName = checkIdentifier();
                        FunctionCall funcCall = parseFunctionCallExpr(funcName);
                        expect(";");
                        return new FunctionCallStmt(funcCall);
                    } else {
                        throw new ParseException("Unexpected identifier in statement: " + first, tokens.get(0));
                    }
                }

                // Любые другие неожиданные токены
                throw new ParseException("Unexpected token in statement: " + first, tokens.get(0));
        }
    }


    private Assignment parseAssign() throws ParseException {
        // СТРОГАЯ ПРОВЕРКА ТОЛЬКО СИНТАКСИСА: слева от := может быть только идентификатор
        if (!isIdentifier(tokens.get(0).getLexeme())) {
            throw new ParseException("Expected variable name before ':='", tokens.get(0));
        }

        Token varName = checkIdentifier();
        expect(":=");

        // Check for arithmetic expressions without parentheses
        if (!peek("(") && isArithmeticExpressionAhead()) {
            throw new ParseException("Arithmetic expressions must be enclosed in parentheses", tokens.get(0));
        }

        Expression expr = parseExpr();

        if (peek(";")) {
            nextToken();
        }

        return new Assignment(varName, expr);
    }

    private boolean isArithmeticExpressionAhead() {
        List<Token> tempTokens = new ArrayList<>(tokens);
        int parenCount = 0;

        for (int i = 0; i < Math.min(10, tempTokens.size()); i++) {
            Token tok = tempTokens.get(i);
            String lexeme = tok.getLexeme();

            if (lexeme.equals("(")) {
                parenCount++;
            } else if (lexeme.equals(")")) {
                parenCount--;
            } else if (parenCount == 0 &&
                    (lexeme.equals("+") || lexeme.equals("-") ||
                            lexeme.equals("*") || lexeme.equals("/") || lexeme.equals("%"))) {
                return true;
            } else if (lexeme.equals(";") || lexeme.equals("end") ||
                    lexeme.equals("else")) {
                break;
            }

            if (parenCount < 0) break;
        }

        return false;
    }

    private IfThenElse parseIf() throws ParseException {
        Token ifToken = expect("if");

        // Проверка, что условие не пустое
        if (tokens.isEmpty() || peek("then") || peek(";") || peek("end")) {
            throw new ParseException("Expected condition after 'if'", ifToken);
        }

        Expression condition = parseExpr();

        // СТРОГАЯ проверка для 'then'
        if (tokens.isEmpty()) {
            throw new ParseException("Expected 'then' after if condition", ifToken);
        }

        Token nextToken = tokens.get(0);
        if (!nextToken.getLexeme().equals("then")) {
            throw new ParseException("Expected 'then' after if condition, got '" + nextToken.getLexeme() + "'", nextToken);
        }

        expect("then");

        List<Statement> thenStmts = parseStmts();
        List<Statement> elseStmts = new ArrayList<>();

        if (peek("else")) {
            expect("else");
            elseStmts = parseStmts();
        }

        expect("end");

        if (peek("if")) {
            nextToken();
        }

        if (peek(";")) {
            nextToken();
        }

        return new IfThenElse(ifToken, condition, thenStmts, elseStmts);
    }

    private WhileLoop parseWhile() throws ParseException {
        Token whileToken = expect("while");
        Expression condition = parseExpr();

        // Allow both 'do' and 'loop' in while loops
        if (peek("do") || peek("loop")) {
            nextToken(); // consume "do" or "loop"
        } else {
            throw new ParseException("Expected 'do' or 'loop' after while condition", tokens.get(0));
        }

        List<Statement> bodyStmts = parseStmts();
        expect("end");

        if (peek("while") || peek("loop")) {
            nextToken();
        }

        if (peek(";")) {
            nextToken();
        }

        return new WhileLoop(whileToken, condition, bodyStmts);
    }


    private PrintStmt parsePrint() throws ParseException {
        Token printToken = nextToken();
        Expression expr = null;

        boolean isPrintLine = printToken.getLexeme().equals("print_line");

        // Parse expression if present (optional for print_line)
        if (!tokens.isEmpty() && !peek(";") && !peek("end") && !peek("else")) {
            expr = parseExpr();
        } else if (!isPrintLine) {
            // print requires an argument
            throw new ParseException("print requires an argument", printToken);
        }

        if (peek(";")) {
            nextToken();
        }

        return new PrintStmt(printToken, expr);
    }


    private ReturnStmt parseReturn() throws ParseException {
        Token returnToken = expect("return");
        Expression expr = null;

        if (!peek(";")) {
            expr = parseExpr();
        }

        if (peek(";")) {
            try {
                nextToken();
            } catch (ParseException e) {
                // ignore
            }
        }

        return new ReturnStmt(returnToken, expr);
    }

    private Block parseBlock() throws ParseException {
        Token beginToken = expect("begin");
        List<Statement> stmts = parseStmts();
        expect("end");

        if (peek(";")) {
            try {
                nextToken();
            } catch (ParseException e) {
                // ignore
            }
        }

        return new Block(beginToken, stmts);
    }

    /*-----------------------------------------
     * Expressions
     *----------------------------------------*/
    private Expression parseExpr() throws ParseException {
        if (tokens.isEmpty()) {
            throw new ParseException("Unexpected end of input in expression", null);
        }

        // Проверка, что выражение не начинается с недопустимого токена
        String first = tokens.get(0).getLexeme();
        if (first.equals(";") || first.equals("end") || first.equals("else") ||
                first.equals("then") || first.equals("do") || first.equals("loop")) {
            throw new ParseException("Expected expression, got '" + first + "'", tokens.get(0));
        }

        return parseOrExpr();
    }

    private Expression parseOrExpr() throws ParseException {
        Expression left = parseAndExpr();

        while (peek("or")) {
            Token op = nextToken();
            Expression right = parseAndExpr();
            left = new BinaryOp(left, op, right);
        }

        return left;
    }

    private Expression parseAndExpr() throws ParseException {
        Expression left = parseComparison();

        while (peek("and")) {
            Token op = nextToken();
            Expression right = parseComparison();
            left = new BinaryOp(left, op, right);
        }

        return left;
    }

    private Expression parseComparison() throws ParseException {
        Expression left = parseAddSub();

        while (peek("<") || peek("<=") || peek(">") || peek(">=") || peek("==") || peek("!=")) {
            Token op = nextToken();
            Expression right = parseAddSub();
            left = new BinaryOp(left, op, right);
        }

        return left;
    }

    private Expression parseAddSub() throws ParseException {
        Expression left = parseMulDiv();

        while (peek("+") || peek("-")) {
            Token op = nextToken();
            Expression right = parseMulDiv();
            left = new BinaryOp(left, op, right);
        }

        return left;
    }

    private Expression parseMulDiv() throws ParseException {
        Expression left = parseFactor();

        while (peek("*") || peek("/") || peek("%")) {
            Token op = nextToken();
            Expression right = parseFactor();
            left = new BinaryOp(left, op, right);
        }

        return left;
    }

    // ======================
// parseFactor() для Phase 2
// ======================
    private Expression parseFactor() throws ParseException {
        if (tokens.isEmpty()) {
            throw new ParseException("Unexpected end of input in factor", null);
        }

        Token token = tokens.get(0);
        String lexeme = token.getLexeme();

        if (lexeme.equals("-") || lexeme.equals("not")) {
            Token op = nextToken();
            Expression expr = parseFactor();
            return new UnaryOp(op, expr);
        }

        if (lexeme.equals("(")) {
            nextToken();
            Expression expr = parseExpr();
            expect(")");
            return expr;
        }

        if (lexeme.matches("\\d+") || (lexeme.startsWith("\"") && lexeme.endsWith("\"")) ||
                lexeme.equals("true") || lexeme.equals("false")) {
            nextToken();
            return new Literal(token);
        }

        if (isIdentifier(lexeme)) {
            nextToken();

            if (peek("(")) {
                return parseFunctionCallExpr(token);
            }

            return new VariableRef(token);
        }

        throw new ParseException("Unexpected token in factor: " + lexeme, token);
    }


    private boolean isArithmeticOperator(String lexeme) {
        return lexeme.equals("+") || lexeme.equals("-") || lexeme.equals("*") ||
                lexeme.equals("/") || lexeme.equals("%");
    }

    private FunctionCall parseFunctionCallExpr(Token funcName) throws ParseException {
        expect("(");
        List<Expression> args = new ArrayList<>();package splat.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import splat.lexer.Token;
import splat.parser.elements.*;

        public class Parser {

            private List<Token> tokens;
            private boolean hasErrors = false;

            public Parser(List<Token> tokens) {
                this.tokens = tokens;
            }

            private void debug(String msg) {
                System.err.println("[DEBUG] " + msg);
            }

            /*-----------------------------------------
             * Utility methods
             *----------------------------------------*/

            private Token nextToken() throws ParseException {
                if (tokens.isEmpty())
                    throw new ParseException("Unexpected end of input.", null);
                return tokens.remove(0);
            }

            private Token expect(String expected) throws ParseException {
                if (tokens.isEmpty())
                    throw new ParseException("Unexpected end of input, expected '" + expected + "'", null);
                Token t = tokens.get(0);
                if (!t.getLexeme().equals(expected)) {
                    throw new ParseException("Expected '" + expected + "', got '" + t.getLexeme() + "'.", t);
                }
                return tokens.remove(0);
            }

            private boolean peek(String expected) {
                return !tokens.isEmpty() && tokens.get(0).getLexeme().equals(expected);
            }

            private boolean match(String expected) throws ParseException {
                if (peek(expected)) {
                    nextToken();
                    return true;
                }
                return false;
            }

            private void skipStrayTokens() {
                while (!tokens.isEmpty() && (peek(";") || peek(":"))) {
                    tokens.remove(0);
                }
            }

            private static final Set<String> KEYWORDS = new HashSet<>();
            static {
                String[] keys = { "if", "then", "else", "while", "loop", "do",
                        "begin", "end", "program", "return", "is",
                        "print", "print_line", "not" };
                for (String k : keys) KEYWORDS.add(k);
            }

            private Token checkIdentifier() throws ParseException {
                if (tokens.isEmpty())
                    throw new ParseException("Unexpected end of input, expected identifier.", null);
                Token tok = tokens.get(0);
                if (!isIdentifier(tok.getLexeme())) {
                    throw new ParseException("Expected identifier, got '" + tok.getLexeme() + "'", tok);
                }
                return tokens.remove(0);
            }

            private boolean isIdentifier(String val) {
                return val != null && !val.isEmpty() && !KEYWORDS.contains(val) && val.matches("[A-Za-z_][A-Za-z0-9_]*");
            }

            private void skipUntil(String... stopTokens) {
                while (!tokens.isEmpty()) {
                    String current = tokens.get(0).getLexeme();
                    for (String stop : stopTokens) {
                        if (current.equals(stop)) {
                            return;
                        }
                    }
                    tokens.remove(0);
                }
            }

            /*-----------------------------------------
             * Program
             *----------------------------------------*/
            public ProgramAST parse() throws ParseException {
                if (!tokens.isEmpty()) {
                    String firstLexeme = tokens.get(0).getLexeme();
                    // Логируем первые 10 токенов для диагностики
                    System.err.println("DEBUG PARSER START - First 10 tokens: " +
                            tokens.subList(0, Math.min(10, tokens.size())));
                }

                try {
                    expect("program");

                    // Program name
                    Token progName;
                    if (!tokens.isEmpty() && isIdentifier(tokens.get(0).getLexeme())) {
                        progName = checkIdentifier();
                    } else {
                        progName = new Token("main", tokens.isEmpty() ? 0 : tokens.get(0).getLine(),
                                tokens.isEmpty() ? 0 : tokens.get(0).getCol());
                    }

                    skipStrayTokens();

                    List<Declaration> decls = parseDecls();
                    expect("begin");
                    List<Statement> stmts = parseStmts();
                    expect("end");

                    if (peek(";")) {
                        try {
                            nextToken();
                        } catch (ParseException e) {
                            // ignore
                        }
                    }

                    return new ProgramAST(decls, stmts, progName);
                } catch (ParseException e) {
                    hasErrors = true;
                    throw e;
                }
            }

            /*-----------------------------------------
             * Declarations
             *----------------------------------------*/
            private List<Declaration> parseDecls() {
                List<Declaration> decls = new ArrayList<>();
                Set<String> declaredNames = new HashSet<>();

                while (!tokens.isEmpty() && !peek("begin")) {
                    skipStrayTokens();
                    if (tokens.isEmpty() || peek("begin")) break;

                    if (isIdentifier(tokens.get(0).getLexeme())) {
                        try {
                            String potentialName = tokens.get(0).getLexeme();
                            if (declaredNames.contains(potentialName)) {
                                throw new ParseException("Duplicate declaration: '" + potentialName + "'", tokens.get(0));
                            }

                            Declaration decl = parseDecl();
                            if (decl != null) {
                                decls.add(decl);
                                declaredNames.add(potentialName);
                            }
                        } catch (ParseException e) {
                            skipUntil("begin", "end", ";");
                        }
                    } else {
                        if (!peek("begin")) {
                            tokens.remove(0);
                        }
                    }
                }

                return decls;
            }

            private Declaration parseDecl() throws ParseException {
                System.err.println("DEBUG parseDecl: tokens = " + tokens.subList(0, Math.min(5, tokens.size())));

                Token nameTok = checkIdentifier();
                System.err.println("DEBUG: nameTok = " + nameTok.getLexeme());

                if (tokens.isEmpty()) {
                    throw new ParseException("Unexpected end of input after identifier", nameTok);
                }

                Token next = tokens.get(0);
                System.err.println("DEBUG: next token = " + next.getLexeme());

                if (next.getLexeme().equals(":")) {
                    System.err.println("DEBUG: parsing variable declaration");
                    return parseVariableDecl();
                } else if (next.getLexeme().equals("is")) {
                    System.err.println("DEBUG: parsing function without params");
                    return parseFunctionDeclNoParams(nameTok);
                } else if (next.getLexeme().equals("(")) {
                    System.err.println("DEBUG: parsing function with params");
                    return parseFunctionDecl(nameTok);
                } else {
                    System.err.println("DEBUG: throwing parse exception");
                    throw new ParseException("Expected ':' or 'is' after identifier, got '" + next.getLexeme() + "'", next);
                }
            }


            private VariableDecl parseVariableDecl() throws ParseException {
                Token nameTok = checkIdentifier();
                expect(":");
                Token typeTok = checkIdentifier();

                if (peek(";")) {
                    nextToken();
                }

                return new VariableDecl(nameTok, typeTok);
            }




            private FunctionDecl parseFunctionDecl(Token nameTok) throws ParseException {
                expect("(");
                List<VariableDecl> params = parseParamList();

                Token returnType = null;
                if (peek(":")) {
                    nextToken();
                    returnType = checkIdentifier();
                }

                expect("is");
                expect("begin");

                List<VariableDecl> localVars = new ArrayList<>();

                // НА Phase 2 НЕ ПРОВЕРЯЕМ ДУБЛИРОВАНИЕ С ПАРАМЕТРАМИ - это для Phase 3
                while (!tokens.isEmpty() && isIdentifier(tokens.get(0).getLexeme()) &&
                        tokens.size() > 1 && tokens.get(1).getLexeme().equals(":")) {
                    localVars.add(parseVariableDecl());
                }

                List<Statement> bodyStmts = parseStmts();

                // НА Phase 2 НЕ ПРОВЕРЯЕМ НАЛИЧИЕ RETURN - это для Phase 3
                expect("end");

                if (!tokens.isEmpty() && tokens.get(0).getLexeme().equals(nameTok.getLexeme())) {
                    nextToken();
                }

                if (peek(";")) nextToken();

                return new FunctionDecl(nameTok, params, returnType, localVars, bodyStmts);
            }


            private FunctionDecl parseFunctionDeclNoParams(Token nameTok) throws ParseException {
                expect("is");
                expect("begin");

                List<VariableDecl> localVars = new ArrayList<>();
                while (!tokens.isEmpty() && isIdentifier(tokens.get(0).getLexeme()) &&
                        tokens.size() > 1 && tokens.get(1).getLexeme().equals(":")) {
                    localVars.add(parseVariableDecl());
                }

                List<Statement> bodyStmts = parseStmts();
                expect("end");

                // Опциональное имя функции после end
                if (!tokens.isEmpty() && tokens.get(0).getLexeme().equals(nameTok.getLexeme())) {
                    nextToken();
                }

                if (peek(";")) nextToken();

                return new FunctionDecl(nameTok, new ArrayList<>(), null, localVars, bodyStmts);
            }

            private List<VariableDecl> parseParamList() throws ParseException {
                List<VariableDecl> params = new ArrayList<>();
                expect("(");

                if (!peek(")")) {
                    do {
                        Token paramName = checkIdentifier();
                        expect(":");
                        Token paramType = checkIdentifier();
                        params.add(new VariableDecl(paramName, paramType));
                    } while (match(","));
                }

                expect(")");
                return params;
            }

            /*-----------------------------------------
             * Statements - IMPROVED VERSION
             *----------------------------------------*/
            private List<Statement> parseStmts() throws ParseException {
                List<Statement> stmts = new ArrayList<>();

                while (!tokens.isEmpty()) {
                    String current = tokens.get(0).getLexeme();

                    if (current.equals("end") || current.equals("else")) {
                        break;
                    }

                    if (current.equals(";")) {
                        tokens.remove(0);
                        continue;
                    }

                    // Проверка на объявления переменных в statements section
                    if (isIdentifier(current) && tokens.size() > 1 && tokens.get(1).getLexeme().equals(":")) {
                        throw new ParseException("Variable declarations are not allowed in statements section", tokens.get(0));
                    }

                    // Проверка на другие недопустимые конструкции
                    if (current.equals(":")) {
                        throw new ParseException("Unexpected ':' in statements section", tokens.get(0));
                    }

                    // Проверка на недопустимые ключевые слова в statements
                    if (current.equals("program") || current.equals("is")) {
                        throw new ParseException("Unexpected keyword in statements section: " + current, tokens.get(0));
                    }

                    Statement stmt = parseStmt();
                    stmts.add(stmt);
                }

                return stmts;
            }

            // ======================
// parseStmt() для Phase 2
// ======================
            private Statement parseStmt() throws ParseException {
                if (tokens.isEmpty()) {
                    throw new ParseException("Unexpected end of input in statement", null);
                }

                String first = tokens.get(0).getLexeme();

                switch (first) {
                    case "if":
                        return parseIf();
                    case "while":
                        return parseWhile();
                    case "print":
                    case "print_line":
                        return parsePrint();
                    case "return":
                        return parseReturn();
                    case "begin":
                        return parseBlock();
                    default:
                        if (isIdentifier(first)) {
                            if (tokens.size() > 1 && tokens.get(1).getLexeme().equals(":=")) {
                                return parseAssign();
                            } else if (tokens.size() > 1 && tokens.get(1).getLexeme().equals("(")) {
                                Token funcName = checkIdentifier();
                                FunctionCall funcCall = parseFunctionCallExpr(funcName);
                                expect(";");
                                return new FunctionCallStmt(funcCall);
                            } else {
                                throw new ParseException("Unexpected identifier in statement: " + first, tokens.get(0));
                            }
                        }

                        // Любые другие неожиданные токены
                        throw new ParseException("Unexpected token in statement: " + first, tokens.get(0));
                }
            }


            private Assignment parseAssign() throws ParseException {
                // СТРОГАЯ ПРОВЕРКА ТОЛЬКО СИНТАКСИСА: слева от := может быть только идентификатор
                if (!isIdentifier(tokens.get(0).getLexeme())) {
                    throw new ParseException("Expected variable name before ':='", tokens.get(0));
                }

                Token varName = checkIdentifier();
                expect(":=");

                // Check for arithmetic expressions without parentheses
                if (!peek("(") && isArithmeticExpressionAhead()) {
                    throw new ParseException("Arithmetic expressions must be enclosed in parentheses", tokens.get(0));
                }

                Expression expr = parseExpr();

                if (peek(";")) {
                    nextToken();
                }

                return new Assignment(varName, expr);
            }

            private boolean isArithmeticExpressionAhead() {
                List<Token> tempTokens = new ArrayList<>(tokens);
                int parenCount = 0;

                for (int i = 0; i < Math.min(10, tempTokens.size()); i++) {
                    Token tok = tempTokens.get(i);
                    String lexeme = tok.getLexeme();

                    if (lexeme.equals("(")) {
                        parenCount++;
                    } else if (lexeme.equals(")")) {
                        parenCount--;
                    } else if (parenCount == 0 &&
                            (lexeme.equals("+") || lexeme.equals("-") ||
                                    lexeme.equals("*") || lexeme.equals("/") || lexeme.equals("%"))) {
                        return true;
                    } else if (lexeme.equals(";") || lexeme.equals("end") ||
                            lexeme.equals("else")) {
                        break;
                    }

                    if (parenCount < 0) break;
                }

                return false;
            }

            private IfThenElse parseIf() throws ParseException {
                Token ifToken = expect("if");

                // Проверка, что условие не пустое
                if (tokens.isEmpty() || peek("then") || peek(";") || peek("end")) {
                    throw new ParseException("Expected condition after 'if'", ifToken);
                }

                Expression condition = parseExpr();

                // СТРОГАЯ проверка для 'then'
                if (tokens.isEmpty()) {
                    throw new ParseException("Expected 'then' after if condition", ifToken);
                }

                Token nextToken = tokens.get(0);
                if (!nextToken.getLexeme().equals("then")) {
                    throw new ParseException("Expected 'then' after if condition, got '" + nextToken.getLexeme() + "'", nextToken);
                }

                expect("then");

                List<Statement> thenStmts = parseStmts();
                List<Statement> elseStmts = new ArrayList<>();

                if (peek("else")) {
                    expect("else");
                    elseStmts = parseStmts();
                }

                expect("end");

                if (peek("if")) {
                    nextToken();
                }

                if (peek(";")) {
                    nextToken();
                }

                return new IfThenElse(ifToken, condition, thenStmts, elseStmts);
            }

            private WhileLoop parseWhile() throws ParseException {
                Token whileToken = expect("while");
                Expression condition = parseExpr();

                // Allow both 'do' and 'loop' in while loops
                if (peek("do") || peek("loop")) {
                    nextToken(); // consume "do" or "loop"
                } else {
                    throw new ParseException("Expected 'do' or 'loop' after while condition", tokens.get(0));
                }

                List<Statement> bodyStmts = parseStmts();
                expect("end");

                if (peek("while") || peek("loop")) {
                    nextToken();
                }

                if (peek(";")) {
                    nextToken();
                }

                return new WhileLoop(whileToken, condition, bodyStmts);
            }


            private PrintStmt parsePrint() throws ParseException {
                Token printToken = nextToken();
                Expression expr = null;

                boolean isPrintLine = printToken.getLexeme().equals("print_line");

                // Parse expression if present (optional for print_line)
                if (!tokens.isEmpty() && !peek(";") && !peek("end") && !peek("else")) {
                    expr = parseExpr();
                } else if (!isPrintLine) {
                    // print requires an argument
                    throw new ParseException("print requires an argument", printToken);
                }

                if (peek(";")) {
                    nextToken();
                }

                return new PrintStmt(printToken, expr);
            }


            private ReturnStmt parseReturn() throws ParseException {
                Token returnToken = expect("return");
                Expression expr = null;

                if (!peek(";")) {
                    expr = parseExpr();
                }

                if (peek(";")) {
                    try {
                        nextToken();
                    } catch (ParseException e) {
                        // ignore
                    }
                }

                return new ReturnStmt(returnToken, expr);
            }

            private Block parseBlock() throws ParseException {
                Token beginToken = expect("begin");
                List<Statement> stmts = parseStmts();
                expect("end");

                if (peek(";")) {
                    try {
                        nextToken();
                    } catch (ParseException e) {
                        // ignore
                    }
                }

                return new Block(beginToken, stmts);
            }

            /*-----------------------------------------
             * Expressions
             *----------------------------------------*/
            private Expression parseExpr() throws ParseException {
                if (tokens.isEmpty()) {
                    throw new ParseException("Unexpected end of input in expression", null);
                }

                // Проверка, что выражение не начинается с недопустимого токена
                String first = tokens.get(0).getLexeme();
                if (first.equals(";") || first.equals("end") || first.equals("else") ||
                        first.equals("then") || first.equals("do") || first.equals("loop")) {
                    throw new ParseException("Expected expression, got '" + first + "'", tokens.get(0));
                }

                return parseOrExpr();
            }

            private Expression parseOrExpr() throws ParseException {
                Expression left = parseAndExpr();

                while (peek("or")) {
                    Token op = nextToken();
                    Expression right = parseAndExpr();
                    left = new BinaryOp(left, op, right);
                }

                return left;
            }

            private Expression parseAndExpr() throws ParseException {
                Expression left = parseComparison();

                while (peek("and")) {
                    Token op = nextToken();
                    Expression right = parseComparison();
                    left = new BinaryOp(left, op, right);
                }

                return left;
            }

            private Expression parseComparison() throws ParseException {
                Expression left = parseAddSub();

                while (peek("<") || peek("<=") || peek(">") || peek(">=") || peek("==") || peek("!=")) {
                    Token op = nextToken();
                    Expression right = parseAddSub();
                    left = new BinaryOp(left, op, right);
                }

                return left;
            }

            private Expression parseAddSub() throws ParseException {
                Expression left = parseMulDiv();

                while (peek("+") || peek("-")) {
                    Token op = nextToken();
                    Expression right = parseMulDiv();
                    left = new BinaryOp(left, op, right);
                }

                return left;
            }

            private Expression parseMulDiv() throws ParseException {
                Expression left = parseFactor();

                while (peek("*") || peek("/") || peek("%")) {
                    Token op = nextToken();
                    Expression right = parseFactor();
                    left = new BinaryOp(left, op, right);
                }

                return left;
            }

            // ======================
// parseFactor() для Phase 2
// ======================
            private Expression parseFactor() throws ParseException {
                if (tokens.isEmpty()) {
                    throw new ParseException("Unexpected end of input in factor", null);
                }

                Token token = tokens.get(0);
                String lexeme = token.getLexeme();

                if (lexeme.equals("-") || lexeme.equals("not")) {
                    Token op = nextToken();
                    Expression expr = parseFactor();
                    return new UnaryOp(op, expr);
                }

                if (lexeme.equals("(")) {
                    nextToken();
                    Expression expr = parseExpr();
                    expect(")");
                    return expr;
                }

                if (lexeme.matches("\\d+") || (lexeme.startsWith("\"") && lexeme.endsWith("\"")) ||
                        lexeme.equals("true") || lexeme.equals("false")) {
                    nextToken();
                    return new Literal(token);
                }

                if (isIdentifier(lexeme)) {
                    nextToken();

                    if (peek("(")) {
                        return parseFunctionCallExpr(token);
                    }

                    return new VariableRef(token);
                }

                throw new ParseException("Unexpected token in factor: " + lexeme, token);
            }


            private boolean isArithmeticOperator(String lexeme) {
                return lexeme.equals("+") || lexeme.equals("-") || lexeme.equals("*") ||
                        lexeme.equals("/") || lexeme.equals("%");
            }

            private FunctionCall parseFunctionCallExpr(Token funcName) throws ParseException {
                expect("(");
                List<Expression> args = new ArrayList<>();

                if (!peek(")")) {
                    do {
                        Expression arg = parseExpr();
                        args.add(arg);
                    } while (match(","));
                }

                expect(")");
                return new FunctionCall(funcName, args);
            }
        }

        if (!peek(")")) {
            do {
                Expression arg = parseExpr();
                args.add(arg);
            } while (match(","));
        }

        expect(")");
        return new FunctionCall(funcName, args);
    }
}