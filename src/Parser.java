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
        File sourceCode = new File("input1.txt");

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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        print();
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        if (currentToken == TOKENS.IDENTIFIER) {
            print();
            ArgList();
        } else {
            //output.remove(output.size() - 1);
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
            cursor = temp;
        }

        return SUCCESS();
    }

    static boolean Statements() {
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        print();
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
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
        String out = "";
        for (int i = 0; i < depth; i++) {
            out += "\t";
        }
        out += (currentToken.toString() + " (" + currentLexeme + ")");
        output.add(out);
    }

    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER}
}
