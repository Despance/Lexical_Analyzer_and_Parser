import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Parser {
    final static boolean SUCCESS = true;
    final static boolean FAILURE = false;
    static int depth = 0;

    static TOKENS currentToken;
    static String currentLexeme;
    static int lineNumber = 0;

    static File input;
    static FileReader sourceCode;

    static Scanner readToken;
    static BufferedReader readCode;

    public static void main(String[] args) throws FileNotFoundException {
        input = new File("output.txt");
        sourceCode = new FileReader("input.txt");

        readToken = new Scanner(input);
        readCode = new BufferedReader(sourceCode);
    }

    public static boolean lex() {
        try {
            int lineNumber;
            if (readToken.hasNext()) {
                currentToken = TOKENS.valueOf(readToken.next());

                String pos = readToken.next();
                lineNumber = Integer.parseInt(pos.substring(0, pos.indexOf(':')));
            } else
                return false;

            if (lineNumber != Parser.lineNumber + 1) {
                readCode.readLine();
                Parser.lineNumber++;
            }

            int ch;
            currentLexeme = "";
            do {
                ch = readCode.read();
                if (ch == -1)
                    break;
                currentLexeme += (char) ch;
            }
            while (ch != ' ' && !isBracket((char) ch));

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return true;
    }

    static boolean Program() {
        if (TopLevelForm())
            Program();
        return SUCCESS;
    }

    static boolean TopLevelForm() {
        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE;
        if (!SecondLevelForm())
            return FAILURE;
        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE;

        return SUCCESS;
    }

    static boolean SecondLevelForm() {
        if (Definition()) {
            return SUCCESS;
        } else {
            lex();
            if (currentToken != TOKENS.LEFTPAR)
                return FAILURE;

            if (!FunCall())
                return FAILURE;

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                return FAILURE;

            return SUCCESS;
        }
    }

    static boolean Definition() {
        lex();
        if (currentToken != TOKENS.DEFINE)
            return FAILURE;
        if (!DefinitionRight())
            return FAILURE;
        return SUCCESS;
    }

    static boolean DefinitionRight() {
        lex();
        if (currentToken == TOKENS.IDENTIFIER) {
            if (!Expression())
                return FAILURE;
            return SUCCESS;
        }

        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE;
        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE;
        if (!ArgList())
            return FAILURE;
        // WE DONT TO LEX HERE
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE;
        if (!Statements())
            return FAILURE;

        return SUCCESS;

    }

    static boolean ArgList() {
        lex();
        if (currentToken == TOKENS.IDENTIFIER)
            ArgList();
        return SUCCESS;
    }

    static boolean Statements() {
        if (Expression())
            return SUCCESS;

        if (!Definition())
            return FAILURE;
        if (!Statements())
            return FAILURE;

        return SUCCESS;

    }

    static boolean Expressions() {
        if (Expression())
            Expressions();
        return SUCCESS;
    }

    static boolean Expression() {
        lex();
        switch (currentToken) {
            case IDENTIFIER:
                break;
            case NUMBER:
                break;
            case CHAR:
                break;
            case BOOLEAN:
                break;
            case STRING:
                break;
            case LEFTPAR:
                if (Expr()) {
                    lex();
                    if (currentToken != TOKENS.RIGHTPAR) {
                        return FAILURE;
                    }
                } else return FAILURE;

                break;
            default:
                return FAILURE;

        }
        return SUCCESS;
    }


    public static boolean isBracket(char ch) {
        return ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}';
    }

    public static boolean Expr() {
        if (LetExpression())
            return SUCCESS;
        if (CondExpression())
            return SUCCESS;
        if (IfExpression())
            return SUCCESS;
        if (BeginExpression())
            return SUCCESS;
        if (FunCall())
            return SUCCESS;

        return FAILURE;
    }

    public static boolean FunCall() {
        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE;
        if (!Expression())
            return FAILURE;

        return SUCCESS;
    }

    public static boolean LetExpression() {
        lex();
        if (currentToken != TOKENS.LET)
            return FAILURE;
        if (!LetExpr())
            return FAILURE;

        return SUCCESS;
    }

    public static boolean LetExpr() {
        lex();
        if (currentToken == TOKENS.LEFTPAR) {
            if (!VarDefs())
                return FAILURE;

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                return FAILURE;
            if (!Statements())
                return FAILURE;
        } else if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE;
        if (!VarDefs())
            return FAILURE;

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE;
        if (!Statements())
            return FAILURE;

        return SUCCESS;
    }

    public static boolean VarDefs() {
        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE;

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE;

        if (!Expression())
            return FAILURE;

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE;
        if (!VarDef())
            return FAILURE;

        return SUCCESS;
    }

    public static boolean VarDef() {
        VarDefs();

        return SUCCESS;
    }

    public static boolean CondExpression() {
        lex();
        if (currentToken != TOKENS.COND)
            return FAILURE;
        if (!CondBranches())
            return FAILURE;

        return SUCCESS;
    }

    public static boolean CondBranches() {
        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE;
        if (!Expression())
            return FAILURE;
        if (!Statements())
            return FAILURE;

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE;
        if (!CondBranch())
            return FAILURE;

        return SUCCESS;
    }

    public static boolean CondBranch() {
        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return SUCCESS;
        if (!Expression())
            return FAILURE;
        if (!Statements())
            return FAILURE;

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE;
        else
            return SUCCESS;
    }

    public static boolean IfExpression() {
        lex();
        if (currentToken != TOKENS.IF)
            return FAILURE;
        if (!Expression())
            return FAILURE;
        if (!Expression())
            return FAILURE;
        if (!EndExpression())
            return FAILURE;

        return SUCCESS;
    }

    public static boolean EndExpression() {
        Expression();

        return SUCCESS;
    }

    public static boolean BeginExpression() {
        lex();
        if (currentToken != TOKENS.BEGIN)
            return FAILURE;
        if (!Statements())
            return FAILURE;

        return SUCCESS;
    }

    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER}
}
