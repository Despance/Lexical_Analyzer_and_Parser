import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {
    static int depth = 0;

    static TOKENS currentToken;
    static String currentLexeme;

    static ArrayList<String> tokens = new ArrayList<>();
    static ArrayList<String> codeLines = new ArrayList<>();
    static int cursor = 0;

    static boolean SUCCESS() {
        depth--;
        return true;
    }

    static boolean FAILURE() {
        depth--;
        return false;
    }

    public static void main(String[] args) throws FileNotFoundException {
        File input = new File("output.txt");
        File sourceCode = new File("input.txt");

        Scanner sc = new Scanner(input);
        while (sc.hasNextLine()) {
            tokens.add(sc.nextLine());
        }

        sc = new Scanner(sourceCode);
        while (sc.hasNextLine()) {
            codeLines.add(sc.nextLine());
        }

        Program();
    }

    public static boolean lex() {
        if (cursor >= tokens.size())
            return false;

        String[] tokenInfo = tokens.get(cursor).split(" ");
        currentToken = TOKENS.valueOf(tokenInfo[0]);

        int lineNumber = Integer.parseInt(tokenInfo[1].substring(0, tokenInfo[1].indexOf(':'))) - 1;
        int index = Integer.parseInt(tokenInfo[1].substring(tokenInfo[1].indexOf(':') + 1)) - 1;

        cursor++;

        String line = codeLines.get(lineNumber);
        char ch = line.charAt(index);
        currentLexeme = String.valueOf(line.charAt(index));

        if (!isBracket(ch)) {
            for (int i = 1; i < line.length() - index; i++) {
                ch = line.charAt(index + i);
                if (ch == ' ' || isBracket(ch))
                    break;
                currentLexeme += ch;
            }
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
        print();

        if (!SecondLevelForm())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        print();

        return SUCCESS();
    }

    static boolean SecondLevelForm() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        int temp = cursor;
        if (Definition())
            return SUCCESS();

        cursor = temp;
        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        print();

        if (!FunCall())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        print();

        return SUCCESS();
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
        print();

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
            print();
            if (!Expression())
                return FAILURE();

            return SUCCESS();
        }

        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();
        print();

        if (!ArgList())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        print();

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

        int temp = cursor;
        lex();
        if (currentToken == TOKENS.IDENTIFIER) {
            print();
            ArgList();
        } else
            cursor = temp;

        return SUCCESS();
    }

    static boolean Statements() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        int temp = cursor;
        if (Expression())
            return SUCCESS();

        cursor = temp;
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

        int temp = cursor;
        if (Expression())
            Expressions();
        else
            cursor = temp;

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
                print();
                break;
            case NUMBER:
                print();
                break;
            case CHAR:
                print();
                break;
            case BOOLEAN:
                print();
                break;
            case STRING:
                print();
                break;
            case LEFTPAR:
                print();

                if (!Expr())
                    return FAILURE();

                lex();
                if (currentToken != TOKENS.RIGHTPAR)
                    return FAILURE();
                print();
                break;
            default:
                return FAILURE();
        }

        return SUCCESS();
    }

    public static boolean Expr() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        depth++;

        int temp = cursor;
        if (LetExpression())
            return SUCCESS();

        cursor = temp;
        if (CondExpression())
            return SUCCESS();

        cursor = temp;
        if (IfExpression())
            return SUCCESS();

        cursor = temp;
        if (BeginExpression())
            return SUCCESS();

        cursor = temp;
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
        print();

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
        print();

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
            print();
            if (!VarDefs())
                return FAILURE();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                return FAILURE();
            print();

            if (!Statements())
                return FAILURE();
        } else if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        print();

        if (!VarDefs())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        print();

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
        print();

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();
        print();

        if (!Expression())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        print();

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

        int temp = cursor;
        if (!VarDefs())
            cursor = temp;

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
        print();

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
        print();

        if (!Expression())
            return FAILURE();
        if (!Statements())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        print();

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
        print();

        if (!Expression())
            return FAILURE();
        if (!Statements())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        print();

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
        print();

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

        int temp = cursor;
        if (!Expression())
            cursor = temp;

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
        print();

        if (!Statements())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean isBracket(char ch) {
        return ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}';
    }

    public static void print() {
        for (int i = 0; i < depth; i++) {
            System.out.print('\t');
        }
        System.out.println(currentToken.toString() + " (" + currentLexeme + ")");
    }

    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER}
}
