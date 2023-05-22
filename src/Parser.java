import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {
    static int depth = 0;

    static TOKENS currentToken;
    static String currentLexeme;

    static int lineNumber;
    static int index;

    static ArrayList<String> tokens = new ArrayList<>();
    static ArrayList<String> codeLines = new ArrayList<>();
    static int cursor = 0;

    static ArrayList<String> output = new ArrayList<>();


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

        lineNumber = Integer.parseInt(tokenInfo[1].substring(0, tokenInfo[1].indexOf(':'))) - 1;
        index = Integer.parseInt(tokenInfo[1].substring(tokenInfo[1].indexOf(':') + 1)) - 1;

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

    public static void Program() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        if (currentToken == TOKENS.LEFTPAR) {
            TopLevelForm();
            Program();
        } else
            output.add(out.substring(0, out.indexOf('<')) + "\t__");

        depth--;
    }

    public static void TopLevelForm() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            error("'('");
        addOutput();
        SecondLevelForm();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            error("')'");
        addOutput();

        depth--;
    }

    public static void SecondLevelForm() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        if (currentToken == TOKENS.DEFINE) {
            Definition();
        } else if (currentToken == TOKENS.LEFTPAR) {
            addOutput();
            FunCall();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput();
        } else
            error("'DEFINE' or '('");

        depth--;
    }

    public static void Definition() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.DEFINE)
            error("'DEFINE'");
        addOutput();
        DefinitionRight();

        depth--;
    }

    public static void DefinitionRight() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken == TOKENS.IDENTIFIER) {
            addOutput();
            Expression();
            depth--;
            return;
        } else if (currentToken == TOKENS.LEFTPAR) {
            addOutput();

            lex();
            if (currentToken != TOKENS.IDENTIFIER)
                error("'IDENTIFIER'");
            addOutput();
            ArgList();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput();
            Statements();
        } else
            error("'IDENTIFIER' or '('");

        depth--;
    }

    public static void ArgList() {
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
        depth--;
    }

    public static void Statements() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        if (currentToken == TOKENS.IDENTIFIER || currentToken == TOKENS.NUMBER || currentToken == TOKENS.CHAR
                || currentToken == TOKENS.BOOLEAN || currentToken == TOKENS.STRING || currentToken == TOKENS.LEFTPAR)
            Expression();
        else if (currentToken == TOKENS.DEFINE)
            Definition();
        else
            error("'IDENTIFIER' or 'NUMBER' or 'CHAR' or 'BOOLEAN' or 'STRING' or '(' or 'DEFINE'");

        depth--;
    }

    public static void Expressions() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        if (currentToken == TOKENS.IDENTIFIER || currentToken == TOKENS.NUMBER || currentToken == TOKENS.CHAR
                || currentToken == TOKENS.BOOLEAN || currentToken == TOKENS.STRING || currentToken == TOKENS.LEFTPAR) {
            Expression();
            Expressions();
        } else {
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
        }
        depth--;
    }

    public static void Expression() {
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
                Expr();

                lex();
                if (currentToken != TOKENS.RIGHTPAR)
                    error("')'");
                addOutput();
                break;
            default:
                error("'IDENTIFIER' or 'NUMBER' or 'CHAR' or 'BOOLEAN' or 'STRING' or 'LEFTPAR'");
                break;
        }
        depth--;
    }

    public static void Expr() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;

        switch (currentToken) {
            case LET:
                LetExpression();
                break;
            case COND:
                CondExpression();
                break;
            case IF:
                IfExpression();
                break;
            case BEGIN:
                BeginExpression();
                break;
            case IDENTIFIER:
                FunCall();
                break;
            default:
                error("'LET' or 'COND' or 'IF' or 'BEGIN' or 'IDENTIFIER'");
                break;
        }
        depth--;
    }

    public static void FunCall() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            error("'IDENTIFIER'");
        addOutput();
        Expressions();

        depth--;
    }

    public static void LetExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LET)
            error("'LET'");
        addOutput();
        LetExpr();

        depth--;
    }

    public static void LetExpr() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken == TOKENS.LEFTPAR) {
            addOutput();
            VarDefs();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput();
            Statements();
        } else if (currentToken == TOKENS.IDENTIFIER) {
            addOutput();

            lex();
            if (currentToken != TOKENS.LEFTPAR)
                error("'('");
            addOutput();
            VarDefs();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput();
            Statements();
        } else
            error("'(' or 'IDENTIFIER'");

        depth--;
    }

    public static void VarDefs() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            error("'('");
        addOutput();

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            error("'IDENTIFIER'");
        addOutput();
        Expression();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            error("')'");
        addOutput();
        VarDef();

        depth--;
    }

    public static void VarDef() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        if (currentToken == TOKENS.LEFTPAR) {
            VarDefs();
        } else {
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
        }
        depth--;
    }

    public static void CondExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.COND)
            error("'COND'");
        addOutput();
        CondBranches();

        depth--;
    }

    public static void CondBranches() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LEFTPAR)
            error("'('");
        addOutput();
        Expression();
        Statements();

        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            error("')'");
        addOutput();
        CondBranch();

        depth--;
    }

    public static void CondBranch() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken == TOKENS.LEFTPAR) {
            addOutput();
            Expression();
            Statements();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput();
        } else {
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
        }
        depth--;
    }

    public static void IfExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.IF)
            error("'IF'");
        addOutput();
        Expression();
        Expression();
        EndExpression();

        depth--;
    }

    public static void EndExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        if (currentToken == TOKENS.IDENTIFIER || currentToken == TOKENS.NUMBER || currentToken == TOKENS.CHAR
                || currentToken == TOKENS.BOOLEAN || currentToken == TOKENS.STRING || currentToken == TOKENS.LEFTPAR)
            Expression();
        else {
            output.add(out.substring(0, out.indexOf('<')) + "\t__");
        }
        depth--;
    }

    public static void BeginExpression() {
        String out = addTab();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.BEGIN)
            error("'BEGIN'");
        addOutput();
        Statements();

        depth--;
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

    public static void error(String expected) {
        String errorMessage = String.format("SYNTAX ERROR [%d:%d]: %s is expected", lineNumber + 1, index + 1, expected);
        output.add(errorMessage);
        for (String strings : output) {
            System.out.println(strings);
        }
        System.exit(0);
    }

    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER}
}