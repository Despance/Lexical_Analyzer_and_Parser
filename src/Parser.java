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

    public static boolean isBracket(char ch) {
        return ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}';
    }

    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER}
}
