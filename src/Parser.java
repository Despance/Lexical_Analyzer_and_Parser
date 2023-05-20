import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Parser {
    static int depth = 0;
    static TOKENS currentToken;
    static String currentLexeme;
    static int lineNumber = 0;
    static File input;
    static FileReader sourceCode;
    static Scanner readToken;
    static BufferedReader readCode;

    static boolean SUCCESS() {
        depth--;
        return true;
    }

    static boolean FAILURE() {
        depth--;
        return false;
    }

    public static void main(String[] args) throws FileNotFoundException {
        input = new File("output.txt");
        sourceCode = new FileReader("input.txt");

        readToken = new Scanner(input);
        readCode = new BufferedReader(sourceCode);

        Program();
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
            for (int i = 0; i < depth; i++) {
                System.out.print('\t');
            }
            System.out.println(currentToken.toString() + " (" + currentLexeme + ")");

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return true;
    }

    static boolean Program() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");


        depth++;
        if (TopLevelForm())
            Program();
        return SUCCESS();
    }


    static boolean TopLevelForm() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;
        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        if (!SecondLevelForm())
            return FAILURE();
        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        return SUCCESS();
    }

    static boolean SecondLevelForm() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        if (Definition()) {
            return SUCCESS();
        } else {
            lex();
            if (currentToken != TOKENS.LEFTPAR)
                return FAILURE();

            if (!FunCall())
                return FAILURE();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                return FAILURE();

            return SUCCESS();
        }
    }

    static boolean Definition() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken != TOKENS.DEFINE)
            return FAILURE();
        if (!DefinitionRight())
            return FAILURE();
        return SUCCESS();
    }

    static boolean DefinitionRight() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken == TOKENS.IDENTIFIER) {
            if (!Expression())
                return FAILURE();
            return SUCCESS();
        }

        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();
        if (!ArgList())
            return FAILURE();
        // WE DON'T TO LEX HERE
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        if (!Statements())
            return FAILURE();

        return SUCCESS();

    }

    static boolean ArgList() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken == TOKENS.IDENTIFIER)
            ArgList();
        return SUCCESS();
    }

    static boolean Statements() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        if (Expression())
            return SUCCESS();

        if (!Definition())
            return FAILURE();
        if (!Statements())
            return FAILURE();

        return SUCCESS();

    }

    static boolean Expressions() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        if (Expression())
            Expressions();
        return SUCCESS();
    }

    static boolean Expression() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

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
                if (!Expr())
                    return FAILURE();
                lex();
                if (currentToken != TOKENS.RIGHTPAR) {
                    return FAILURE();
                }


                break;
            default:
                return FAILURE();

        }
        return SUCCESS();
    }


    public static boolean isBracket(char ch) {
        return ch
                == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}';
    }

    public static boolean Expr() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        if (LetExpression())
            return SUCCESS();
        if (CondExpression())
            return SUCCESS();
        if (IfExpression())
            return SUCCESS();
        if (BeginExpression())
            return SUCCESS();
        if (FunCall())
            return SUCCESS();

        return FAILURE();
    }

    public static boolean FunCall() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();
        if (!Expressions())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean LetExpression() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken != TOKENS.LET)
            return FAILURE();
        if (!LetExpr())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean LetExpr() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken == TOKENS.LEFTPAR) {
            if (!VarDefs())
                return FAILURE();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                return FAILURE();
            if (!Statements())
                return FAILURE();
        } else if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        if (!VarDefs())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        if (!Statements())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean VarDefs() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();

        if (!Expression())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        if (!VarDef())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean VarDef() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        VarDefs();

        return SUCCESS();
    }

    public static boolean CondExpression() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken != TOKENS.COND)
            return FAILURE();
        if (!CondBranches())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean CondBranches() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        if (!Expression())
            return FAILURE();
        if (!Statements())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        if (!CondBranch())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean CondBranch() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return SUCCESS();
        if (!Expression())
            return FAILURE();
        if (!Statements())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        else
            return SUCCESS();
    }

    public static boolean IfExpression() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken != TOKENS.IF)
            return FAILURE();
        if (!Expression())
            return FAILURE();
        if (!Expression())
            return FAILURE();
        if (!EndExpression())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean EndExpression() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        Expression();

        return SUCCESS();
    }

    public static boolean BeginExpression() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        lex();
        if (currentToken != TOKENS.BEGIN)
            return FAILURE();
        if (!Statements())
            return FAILURE();

        return SUCCESS();
    }

    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER}
}
