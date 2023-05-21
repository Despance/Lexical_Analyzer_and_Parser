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

    static ArrayList<String> output = new ArrayList<>();

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

        for (String strings : output) {
            System.out.println(strings);
        }
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

        if (currentToken == TOKENS.STRING) {
            for (int i = 1; i < line.length() - index; i++) {
                ch = line.charAt(index + i);
                currentLexeme += ch;
                if (ch == '"' && currentLexeme.charAt(currentLexeme.length() - 2) != '\\')
                    break;
            }
        } else if (!isBracket(ch)) {
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
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        if (TopLevelForm())
            Program();
        else {
            output.remove(output.size() - 1);
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
        }
        return SUCCESS();
    }

    static boolean TopLevelForm() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        addOutput();

        if (!SecondLevelForm())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        addOutput();

        return SUCCESS();
    }

    static boolean SecondLevelForm() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        if (Definition())
            return SUCCESS();

        cursor = temp;
        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        addOutput();

        if (!FunCall())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        addOutput();

        return SUCCESS();
    }

    static boolean Definition() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.DEFINE)
            return FAILURE();
        addOutput();

        if (!DefinitionRight())
            return FAILURE();

        return SUCCESS();
    }

    static boolean DefinitionRight() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken == TOKENS.IDENTIFIER) {
            addOutput();
            if (!Expression())
                return FAILURE();

            return SUCCESS();
        }

        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        addOutput();

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();
        addOutput();

        if (!ArgList())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        addOutput();

        if (!Statements())
            return FAILURE();

        return SUCCESS();
    }

    static boolean ArgList() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        if (currentToken == TOKENS.IDENTIFIER) {
            addOutput();
            ArgList();
        } else {
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
            cursor = temp;
        }

        return SUCCESS();
    }

    static boolean Statements() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        if (Expression())
            Expressions();
        else {
            output.remove(output.size() - 1);
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
            cursor = temp;
        }

        return SUCCESS();
    }

    static boolean Expression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        switch (currentToken) {
            case IDENTIFIER:
                addOutput();
                break;
            case NUMBER:
                addOutput();
                break;
            case CHAR:
                addOutput();
                break;
            case BOOLEAN:
                addOutput();
                break;
            case STRING:
                addOutput();
                break;
            case LEFTPAR:
                addOutput();

                if (!Expr())
                    return FAILURE();

                lex();
                if (currentToken != TOKENS.RIGHTPAR)
                    return FAILURE();
                addOutput();
                break;
            default:
                return FAILURE();
        }

        return SUCCESS();
    }

    public static boolean Expr() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        if (LetExpression())
            return SUCCESS();
        output.remove(output.size() - 1);

        cursor = temp;
        if (CondExpression())
            return SUCCESS();
        output.remove(output.size() - 1);

        cursor = temp;
        if (IfExpression())
            return SUCCESS();
        output.remove(output.size() - 1);

        cursor = temp;
        if (BeginExpression())
            return SUCCESS();
        output.remove(output.size() - 1);

        cursor = temp;
        if (FunCall())
            return SUCCESS();
        output.remove(output.size() - 1);

        return FAILURE();
    }

    public static boolean FunCall() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();
        addOutput();

        if (!Expressions())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean LetExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LET)
            return FAILURE();
        addOutput();

        if (!LetExpr())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean LetExpr() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken == TOKENS.LEFTPAR) {
            addOutput();
            if (!VarDefs())
                return FAILURE();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                return FAILURE();
            addOutput();

            if (!Statements())
                return FAILURE();
        } else if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();
        addOutput();

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        addOutput();

        if (!VarDefs())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        addOutput();

        if (!Statements())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean VarDefs() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        addOutput();

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            return FAILURE();
        addOutput();

        if (!Expression())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        addOutput();

        if (!VarDef())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean VarDef() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        if (!VarDefs()) {
            output.remove(output.size() - 1);
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
            cursor = temp;
        }

        return SUCCESS();
    }

    public static boolean CondExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.COND)
            return FAILURE();
        addOutput();

        if (!CondBranches())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean CondBranches() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return FAILURE();
        addOutput();

        if (!Expression())
            return FAILURE();
        if (!Statements())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        addOutput();

        if (!CondBranch())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean CondBranch() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            return SUCCESS();
        addOutput();

        if (!Expression())
            return FAILURE();
        if (!Statements())
            return FAILURE();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            return FAILURE();
        addOutput();

        return SUCCESS();
    }

    public static boolean IfExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.IF)
            return FAILURE();
        addOutput();

        if (!Expression())
            return FAILURE();
        if (!Expression())
            return FAILURE();
        if (!EndExpression())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean EndExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        if (!Expression()) {
            output.remove(output.size() - 1);
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
            cursor = temp;
        }

        return SUCCESS();
    }

    public static boolean BeginExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.BEGIN)
            return FAILURE();
        addOutput();

        if (!Statements())
            return FAILURE();

        return SUCCESS();
    }

    public static boolean isBracket(char ch) {
        return ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}';
    }

    public static void addOutput() {
        String out = addTab();
        out += (currentToken.toString() + " (" + currentLexeme + ")");
        output.add(out);
    }

    public static String addTab() {
        String out = "";
        for (int i = 0; i < depth; i++)
            out += "\t";
        return out;
    }

    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER}
}