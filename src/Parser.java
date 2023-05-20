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
    static FileReader code;

    public static void main(String[] args) throws FileNotFoundException {
        input = new File("output.txt");
        code = new FileReader("input.txt");
    }

    public static boolean lex() {
        try {
            Scanner readToken = new Scanner(input);
            BufferedReader bf = new BufferedReader(code);

            int lineNumber;
            int index;
            if (readToken.hasNext()) {
                currentToken = TOKENS.valueOf(readToken.next());

                String pos = readToken.next();
                lineNumber = Integer.parseInt(pos.substring(0, pos.indexOf(':')));
                index = Integer.parseInt(pos.substring(pos.indexOf(':') + 1));
            } else
                return false;

            if (lineNumber != Parser.lineNumber + 1) {
                bf.readLine();
                Parser.lineNumber++;
            }

            int ch;
            currentLexeme = "";
            do {
                ch = bf.read();
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
