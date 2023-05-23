import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    static FileWriter outputFile;

    public static void main(String[] args) throws IOException {

        String filePath = "input.txt";

        if (args.length == 0) {
            System.out.print("Enter the filepath: ");
            filePath = new Scanner(System.in).nextLine();
        } else if (!args[0].isEmpty()) {
            filePath = args[0];
        }

        Lexical_Analyzer.main(new String[]{filePath});

        File input = new File("tokens.txt");
        File sourceCode = new File(filePath);

        Scanner sc = new Scanner(input);
        while (sc.hasNextLine()) {
            tokens.add(sc.nextLine());
        }

        sc = new Scanner(sourceCode);
        while (sc.hasNextLine()) {
            codeLines.add(sc.nextLine());
        }

        outputFile = new FileWriter(new File("output.txt"));

        if (tokens.get(tokens.size() - 1).split(" ")[0].equals("LEXICAL"))
            output.add(tokens.get(tokens.size() - 1));
        else
            Program();

        printOutput();
    }

    public static void lex() {
        if (cursor >= tokens.size()) {
            currentToken = TOKENS.EOF;
            currentLexeme = "EOF";
            return;
        }

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
    }

    public static void Program() {
        String out = addSpace();
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
            output.add(out.substring(0, out.indexOf('<')) + " __");

        depth--;
    }

    public static void TopLevelForm() {
        String out = addSpace();
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
        String out = addSpace();
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
            error("'define' or '('");

        depth--;
    }

    public static void Definition() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.DEFINE)
            error("'define'");
        addOutput();
        DefinitionRight();

        depth--;
    }

    public static void DefinitionRight() {
        String out = addSpace();
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
                error("'identifier'");
            addOutput();
            ArgList();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput();
            Statements();
        } else
            error("'identifier' or '('");

        depth--;
    }

    public static void ArgList() {
        String out = addSpace();
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
            output.add(out.substring(0, out.indexOf('<')) + " __");
            cursor = temp;
        }
        depth--;
    }

    public static void Statements() {
        String out = addSpace();
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
            error("'identifier' or 'number' or 'char' or 'boolean' or 'string' or '(' or 'define'");

        depth--;
    }

    public static void Expressions() {
        String out = addSpace();
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
            output.add(out.substring(0, out.indexOf('<')) + " __");
        }
        depth--;
    }

    public static void Expression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        switch (currentToken) {
            case IDENTIFIER:
            case NUMBER:
            case CHAR:
            case BOOLEAN:
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
                error("'identifier' or 'number' or 'char' or 'boolean' or 'string' or '('");
                break;
        }
        depth--;
    }

    public static void Expr() {
        String out = addSpace();
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
                error("'let' or 'cond' or 'if' or 'begin' or 'identifier'");
                break;
        }
        depth--;
    }

    public static void FunCall() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.IDENTIFIER)
            error("'identifier'");
        addOutput();
        Expressions();

        depth--;
    }

    public static void LetExpression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.LET)
            error("'let'");
        addOutput();
        LetExpr();

        depth--;
    }

    public static void LetExpr() {
        String out = addSpace();
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
            error("'(' or 'identifier'");

        depth--;
    }

    public static void VarDefs() {
        String out = addSpace();
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
            error("'identifier'");
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
        String out = addSpace();
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
            output.add(out.substring(0, out.indexOf('<')) + " __");
        }
        depth--;
    }

    public static void CondExpression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.COND)
            error("'cond'");
        addOutput();
        CondBranches();

        depth--;
    }

    public static void CondBranches() {
        String out = addSpace();
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
        String out = addSpace();
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
            output.add(out.substring(0, out.indexOf('<')) + " __");
        }
        depth--;
    }

    public static void IfExpression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.IF)
            error("'if'");
        addOutput();
        Expression();
        Expression();
        EndExpression();

        depth--;
    }

    public static void EndExpression() {
        String out = addSpace();
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
            output.add(out.substring(0, out.indexOf('<')) + " __");
        }
        depth--;
    }

    public static void BeginExpression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.BEGIN)
            error("'begin'");
        addOutput();
        Statements();

        depth--;
    }

    public static boolean isBracket(char ch) {
        return ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}';
    }

    public static void addOutput() {
        String out = addSpace();
        out += (currentToken.toString() + " (" + currentLexeme + ")");
        output.add(out);
    }

    public static String addSpace() {
        String out = "";
        for (int i = 0; i < depth; i++)
            out += " ";
        return out;
    }

    public static void error(String expected) {
        String errorMessage = String.format("SYNTAX ERROR [%d:%d]: %s is expected", lineNumber + 1, index + 1, expected);
        output.add(errorMessage);
        printOutput();
        System.exit(0);
    }

    public static void printOutput() {
        for (String strings : output) {
            System.out.println(strings);
            try {
                outputFile.write(strings + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            outputFile.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER, EOF}
}