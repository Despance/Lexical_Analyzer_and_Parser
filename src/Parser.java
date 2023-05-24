/**
 * PPL Project 1_2 - Parser
 * 1-	Mustafa Emir Uyar (Representative) 150120007
 * 2-	Ege Keklikçi 150121029
 * 3-	Umut Özil 150121019
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {
    static int depth = 0; // stores how many whitespaces to add before printing

    static TOKENS currentToken; // stores the current token
    static String currentLexeme; // stores the current lexeme

    static int lineNumber;
    static int index;

    static ArrayList<String> tokens = new ArrayList<>();
    static ArrayList<String> codeLines = new ArrayList<>();
    static int cursor = 0; // cursor for tokens arraylist

    static ArrayList<String> output = new ArrayList<>();
    static FileWriter outputFile;

    public static void main(String[] args) throws IOException {

        String filePath = "input.txt";

        // get file path from command line argument or user input
        if (args.length == 0) {
            System.out.print("Enter the filepath: ");
            filePath = new Scanner(System.in).nextLine();
        } else if (!args[0].isEmpty()) {
            filePath = args[0];
        }

        // run lexical analyzer to get tokens
        Lexical_Analyzer.main(new String[]{filePath});

        File input = new File("tokens.txt");
        File sourceCode = new File(filePath);

        // read tokens line by line and store them into arraylist
        Scanner sc = new Scanner(input);
        while (sc.hasNextLine()) {
            tokens.add(sc.nextLine());
        }

        // read tokens line by line and store them into arraylist
        sc = new Scanner(sourceCode);
        while (sc.hasNextLine()) {
            codeLines.add(sc.nextLine());
        }

        outputFile = new FileWriter(new File("output.txt"));

        // if there is a lexical error, print it and exit
        if (tokens.get(tokens.size() - 1).split(" ")[0].equals("LEXICAL"))
            output.add(tokens.get(tokens.size() - 1));
            // else start parsing
        else
            Program();

        // print the output that constructed while parsing and write it to output.txt
        printOutput();
    }

    /* This function gets next token and lexeme */
    public static void lex() {
        // if there is no more token, set current token to EOF and return
        if (cursor >= tokens.size()) {
            currentToken = TOKENS.EOF;
            currentLexeme = "EOF";
            return;
        }

        String[] tokenInfo = tokens.get(cursor).split(" "); // split the token into two part token name and position
        currentToken = TOKENS.valueOf(tokenInfo[0]);

        lineNumber = Integer.parseInt(tokenInfo[1].substring(0, tokenInfo[1].indexOf(':'))) - 1; // the line number of the token in source code
        index = Integer.parseInt(tokenInfo[1].substring(tokenInfo[1].indexOf(':') + 1)) - 1; // the index of the token in source code

        cursor++;

        String line = codeLines.get(lineNumber);
        char ch = line.charAt(index);
        currentLexeme = String.valueOf(line.charAt(index));

        // get the lexeme represented by the token
        if (currentToken == TOKENS.STRING) {
            // if the token is 'STRING', get the lexeme between double quotes
            for (int i = 1; i < line.length() - index; i++) {
                ch = line.charAt(index + i);
                currentLexeme += ch;
                // if the current character is '"' and the previous character is not '\', it means the lexeme is finished
                if (ch == '"' && currentLexeme.charAt(currentLexeme.length() - 2) != '\\')
                    break;
            }
        }
        // if the token is not any bracket, get the lexeme until the next space or bracket
        else if (!isBracket(ch)) {
            for (int i = 1; i < line.length() - index; i++) {
                ch = line.charAt(index + i);
                if (ch == ' ' || isBracket(ch))
                    break;
                currentLexeme += ch;
            }
        }
    }

    /* <Program> -> ε | <TopLevelForm> <Program> */
    public static void Program() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        // if the first token is a '(', it means the grammar is <Program> -> <TopLevelForm><Program>
        if (currentToken == TOKENS.LEFTPAR) {
            TopLevelForm();
            Program();
        }
        // else the grammar is <Program> -> ε
        else
            output.add(out.substring(0, out.indexOf('<')) + " __");

        depth--;
    }

    /* <TopLevelForm> -> ( <SecondLevelForm> ) */
    public static void TopLevelForm() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        // in this grammar, the first token must be a '(', else it is an error
        lex();
        if (currentToken != TOKENS.LEFTPAR)
            error("'('");
        addOutput(); // print the current token
        SecondLevelForm(); // then call SecondLevelForm grammar

        // after SecondLevelForm grammar, the next token must be a ')', else it is an error
        lex();
        if (currentToken != TOKENS.RIGHTPAR)
            error("')'");
        addOutput(); // print the current token

        depth--;
    }

    /* <SecondLevelForm> -> <Definition> | ( <FunCall> ) */
    public static void SecondLevelForm() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        // if the next token is 'DEFINE', it means the grammar is <SecondLevelForm> -> <Definition>
        if (currentToken == TOKENS.DEFINE) {
            Definition();
        }
        // if the current token is '(', it means the grammar is <SecondLevelForm> -> ( <FunCall> )
        else if (currentToken == TOKENS.LEFTPAR) {
            addOutput(); // print the current token
            FunCall();

            // after FunCall grammar, the next token must be a ')', else it is an error
            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput(); // print the current token
        }
        // if the current token is neither 'DEFINE' nor '(', it is an error
        else
            error("'define' or '('");

        depth--;
    }

    /* <Definition> -> DEFINE <DefinitionRight> */
    public static void Definition() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        // in this grammar first token must be 'DEFINE', else it is an error
        lex();
        if (currentToken != TOKENS.DEFINE)
            error("'define'");
        addOutput(); // print the current token
        DefinitionRight(); // then call DefinitionRight grammar

        depth--;
    }

    /* <DefinitionRight> -> IDENTIFIER <Expression> | ( IDENTIFIER <ArgList> ) <Statements> */
    public static void DefinitionRight() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        // if the current token is an 'IDENTIFIER', it means the grammar is <DefinitionRight> -> IDENTIFIER <Expression>
        lex();
        if (currentToken == TOKENS.IDENTIFIER) {
            addOutput(); // print the current token
            Expression();
        }
        // if the current token is '(', it means the grammar is <DefinitionRight> -> ( IDENTIFIER <ArgList> ) <Statements>
        else if (currentToken == TOKENS.LEFTPAR) {
            addOutput(); // print the current token

            // after '(', the next token must be an 'IDENTIFIER', else it is an error
            lex();
            if (currentToken != TOKENS.IDENTIFIER)
                error("'identifier'");
            addOutput(); // print the current token
            ArgList();

            // after ArgList grammar, the next token must be a ')', else it is an error
            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput(); // print the current token
            Statements();
        }
        // if the current token is neither 'IDENTIFIER' nor '(', it is an error
        else
            error("'identifier' or '('");

        depth--;
    }

    /* <ArgList> -> ε | IDENTIFIER <ArgList> */
    public static void ArgList() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        // if the next token is an 'IDENTIFIER', it means the grammar is <ArgList> -> IDENTIFIER <ArgList>
        if (currentToken == TOKENS.IDENTIFIER) {
            addOutput(); // print the current token
            ArgList();
        }
        // if the current token is not an 'IDENTIFIER', it means the grammar is <ArgList> -> ε
        else {
            output.add(out.substring(0, out.indexOf('<')) + " __");
            cursor = temp;
        }
        depth--;
    }

    /* <Statements> -> <Expression> | <Definition> <Statements> */
    public static void Statements() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        // if the next token is one of 'IDENTIFIER' or 'NUMBER' or 'CHAR' or 'BOOLEAN' or 'STRING' or '('
        // it means the grammar is <Statements> -> <Expression>
        if (currentToken == TOKENS.IDENTIFIER || currentToken == TOKENS.NUMBER || currentToken == TOKENS.CHAR
                || currentToken == TOKENS.BOOLEAN || currentToken == TOKENS.STRING || currentToken == TOKENS.LEFTPAR) {
            Expression();
        }
        // if the next token is the 'DEFINE', it means the grammar is <Statements> -> <Definition> <Statements>
        else if (currentToken == TOKENS.DEFINE) {
            Definition();
            Statements();
        }
        // if the next token is not one of the above, there is an error
        else
            error("'identifier' or 'number' or 'char' or 'boolean' or 'string' or '(' or 'define'");

        depth--;
    }

    /* <Expressions> -> ε | <Expression> <Expressions> */
    public static void Expressions() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        int temp = cursor;
        lex();
        cursor = temp;
        // if the next token is one of 'IDENTIFIER' or 'NUMBER' or 'CHAR' or 'BOOLEAN' or 'STRING' or '('
        // it means the grammar is <Expressions> -> <Expression> <Expressions>
        if (currentToken == TOKENS.IDENTIFIER || currentToken == TOKENS.NUMBER || currentToken == TOKENS.CHAR
                || currentToken == TOKENS.BOOLEAN || currentToken == TOKENS.STRING || currentToken == TOKENS.LEFTPAR) {
            Expression();
            Expressions();
        }
        // if the next token is not one of the above, it means the grammar is <Expressions> -> ε
        else {
            output.add(out.substring(0, out.indexOf('<')) + " __");
        }
        depth--;
    }

    /* <Expression> -> IDENTIFIER | NUMBER | CHAR | BOOLEAN | STRING | ( <Expr> ) */
    public static void Expression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        switch (currentToken) {
            // if the current token is one of 'IDENTIFIER' or 'NUMBER' or 'CHAR' or 'BOOLEAN' or 'STRING', print the current token
            case IDENTIFIER:
            case NUMBER:
            case CHAR:
            case BOOLEAN:
            case STRING:
                addOutput(); // print the current token
                break;
            // if the current token is '(', it means the grammar is <Expression> -> ( <Expr> )
            case LEFTPAR:
                addOutput(); // print the current token
                Expr(); // call Expr

                // after Expr, the next token must be a ')', else it is an error
                lex();
                if (currentToken != TOKENS.RIGHTPAR)
                    error("')'");
                addOutput(); // print the current token
                break;
            // if the current token is not one of the above, it is an error
            default:
                error("'identifier' or 'number' or 'char' or 'boolean' or 'string' or '('");
                break;
        }
        depth--;
    }

    /* <Expr> -> <LetExpression> | <CondExpression> | <IfExpression> | <BeginExpression> | <FunCall> */
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
            // if the next token is 'LET', it means the grammar is <Expr> -> <LetExpression>
            case LET:
                LetExpression(); // call LetExpression
                break;
            // if the next token is 'COND', it means the grammar is <Expr> -> <CondExpression>
            case COND:
                CondExpression(); // call CondExpression
                break;
            // if the next token is 'IF', it means the grammar is <Expr> -> <IfExpression>
            case IF:
                IfExpression(); // call IfExpression
                break;
            // if the next token is 'BEGIN', it means the grammar is <Expr> -> <BeginExpression>
            case BEGIN:
                BeginExpression(); // call BeginExpression
                break;
            // if the next token is 'IDENTIFIER', it means the grammar is <Expr> -> <FunCall>
            case IDENTIFIER:
                FunCall(); // call FunCall
                break;
            // if the next token is not one of the above, it is an error
            default:
                error("'let' or 'cond' or 'if' or 'begin' or 'identifier'");
                break;
        }
        depth--;
    }

    /* <FunCall> -> IDENTIFIER <Expressions> */
    public static void FunCall() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        // if the next token is not 'IDENTIFIER', throw an error
        if (currentToken != TOKENS.IDENTIFIER)
            error("'identifier'");
        addOutput();
        Expressions();

        depth--;
    }

    /* <LetExpression> -> LET <LetExpr> */
    public static void LetExpression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        // if the next token is not 'LET', throw an error
        if (currentToken != TOKENS.LET)
            error("'let'");
        addOutput();
        LetExpr();

        depth--;
    }

    /* <LetExpr> -> ( <VarDefs> <Statements> ) | IDENTIFIER ( <VarDefs> <Statements> ) */
    public static void LetExpr() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        // if the next token is '(', it means the grammar is <LetExpr> -> ( <VarDefs> <Statements> )
        if (currentToken == TOKENS.LEFTPAR) {
            addOutput();
            VarDefs();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput();
            Statements();
        }
        // if the next token is 'IDENTIFIER', it means the grammar is <LetExpr> -> IDENTIFIER ( <VarDefs> <Statements> )
        else if (currentToken == TOKENS.IDENTIFIER) {
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
        }
        // if the next token is not one of the above, throw an error
        else
            error("'(' or 'identifier'");

        depth--;
    }

    /* <VarDefs> -> ( IDENTIFIER <Expression> ) <VarDef> */
    public static void VarDefs() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;


        lex();
        // if the next token is not '(', throw an error
        if (currentToken != TOKENS.LEFTPAR)
            error("'('");
        addOutput();

        lex();
        // if the next token is not 'IDENTIFIER', throw an error
        if (currentToken != TOKENS.IDENTIFIER)
            error("'identifier'");
        addOutput();
        Expression();

        lex();
        // if the next token is not ')', throw an error
        if (currentToken != TOKENS.RIGHTPAR)
            error("')'");
        addOutput();
        VarDef();

        depth--;
    }

    /* <VarDef> -> ε | <VarDefs> */
    public static void VarDef() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        // save the current cursor position to restore it later
        int temp = cursor;
        lex();
        cursor = temp;

        // if the next token is '(', it means the grammar is <VarDef> -> <VarDefs>
        if (currentToken == TOKENS.LEFTPAR) {
            VarDefs();
        }
        // if the next token is not '(', it means the grammar is <VarDef> -> ε
        else
            output.add(out.substring(0, out.indexOf('<')) + " __");

        depth--;
    }

    /* <CondExpression> -> COND <CondBranches> */
    public static void CondExpression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        // if the current token is not 'COND', throw an error
        if (currentToken != TOKENS.COND)
            error("'cond'");

        addOutput(); // add current token to output
        CondBranches(); // call CondBranches

        depth--;
    }

    /* <CondBranches> -> ( <Expression> <Statements> ) <CondBranch> */
    public static void CondBranches() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        // if the next token is not '(', throw an error
        if (currentToken != TOKENS.LEFTPAR)
            error("'('");
        addOutput();
        Expression();
        Statements();

        lex();
        // if the next token is not ')', throw an error
        if (currentToken != TOKENS.RIGHTPAR)
            error("')'");
        addOutput();
        CondBranch();

        depth--;
    }

    /* <CondBranch> -> ε | (<Expression> <Statements>) */
    public static void CondBranch() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        // if the next token is '(', it means the grammar is CondBranch -> (<Expression> <Statements>)
        if (currentToken == TOKENS.LEFTPAR) {
            addOutput();
            // call the grammar rule functions
            Expression();
            Statements();

            lex();
            if (currentToken != TOKENS.RIGHTPAR)
                error("')'");
            addOutput();
        }
        // if the next token is not '(', it means the grammar is CondBranch -> ε
        else
            output.add(out.substring(0, out.indexOf('<')) + " __");

        depth--;
    }

    /* <IfExpression> -> IF <Expression> <Expression> <EndExpression> */
    public static void IfExpression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.IF)
            error("'if'");
        addOutput(); // add current token to output
        // call the grammar rule functions
        Expression();
        Expression();
        EndExpression();

        depth--;
    }

    /* <EndExpression> -> ε | <Expression> */
    public static void EndExpression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        // keep the current token in a temp variable to restore it later
        int temp = cursor;
        lex();
        cursor = temp;

        // check if the current token is an 'IDENTIFIER', 'NUMBER', 'CHAR', 'BOOLEAN', 'STRING' or '('
        if (currentToken == TOKENS.IDENTIFIER || currentToken == TOKENS.NUMBER || currentToken == TOKENS.CHAR
                || currentToken == TOKENS.BOOLEAN || currentToken == TOKENS.STRING || currentToken == TOKENS.LEFTPAR)
            Expression();
            // if the next token is not one of the above, throw an error
        else
            output.add(out.substring(0, out.indexOf('<')) + " __");

        depth--;
    }

    /* <BeginExpression> -> BEGIN <Statements> */
    public static void BeginExpression() {
        String out = addSpace();
        out += ("<" + new Object() {
        }.getClass().getEnclosingMethod().getName() + ">");
        output.add(out);
        depth++;

        lex();
        if (currentToken != TOKENS.BEGIN)//check if the current token is 'begin'
            error("'begin'");
        addOutput(); // print the token and lexeme
        Statements(); // call the Statements function
        depth--;
    }

    // check if the current character is a bracket
    public static boolean isBracket(char ch) {
        return ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}';
    }

    // add the current token and lexeme to the output
    public static void addOutput() {
        String out = addSpace();
        out += (currentToken.toString() + " (" + currentLexeme + ")");
        output.add(out);
    }

    // add space to the output according to the depth of the function call.
    public static String addSpace() {
        String out = "";
        for (int i = 0; i < depth; i++)
            out += " ";
        return out;
    }

    // print the error message and exit the program
    public static void error(String expected) {
        String errorMessage = String.format("SYNTAX ERROR [%d:%d]: %s is expected", lineNumber + 1, index + 1, expected);
        output.add(errorMessage);
        //print the output before exiting because of the error.
        printOutput();
        System.exit(0);
    }

    //print the output to the console and the output file
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

    // define all tokens for simplicity
    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER, EOF}
}