import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    static int lineCount = 1;
    static String[] keywords = {"define", "let", "cond", "if", "begin"}; // reserved keywords
    static ArrayList<String> output = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        try {
            File inputFile = new File("input.txt");
            Scanner input = new Scanner(inputFile);

            // read the input file line by line
            while (input.hasNextLine()) {
                String line = input.nextLine();
                if (line.charAt(0) != '~') // ignore comment lines
                    if (!firstLook(line))
                        break;

                lineCount += 1;
            }

            File outputFile = new File("output.txt");
            FileWriter writer = new FileWriter(outputFile);

            for (int i = 0; i < output.size(); i++) {
                writer.write(output.get(i));
                writer.write("");
            }
            writer.close();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**This function checks the tokens first index and calls functions accordingly*/
    public static boolean firstLook(String line) {
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch != ' ') { // ignore whitespaces
                if (ch == '(')
                    output.add(String.format("LEFTPAR %d:%d", lineCount, (i+1)));
                else if (ch == ')')
                    output.add(String.format("RIGHTPAR %d:%d", lineCount, (i+1)));
                else if (ch == '[')
                    output.add(String.format("LEFTSQUAREB %d:%d", lineCount, (i+1)));
                else if (ch == ']')
                    output.add(String.format("RIGHTSQUAREB %d:%d", lineCount, (i+1)));
                else if (ch == '{')
                    output.add(String.format("LEFTCURLYB %d:%d", lineCount, (i+1)));
                else if (ch == '}')
                    output.add(String.format("RIGHTCURLYB %d:%d", lineCount, (i+1)));
                    // check if it's a single token identifier or a number
                else if (ch == '.' || ch == '+' || ch == '-') {
                    i = isIdentifier(line, i);
                    if (i == -1) // lexical error
                        return false;
                }
                // check for identifier token
                else if (ch == '!' || ch == '*' | ch == '/' || ch == ':' || ('<' <= ch && ch <= '?') || ('a' <= ch && ch <= 'z')) {
                    i = isIdentifier(line, i);
                    if (i == -1) // lexical error
                        return false;
                }
                // check for number token
                else if (Character.isDigit(ch)) {
                    i = isNumber(line, i); // next tokens index
                    if (i == -1) // lexical error
                        return false;
                }
                // check for character token
                else if (ch == '\'') {
                    i = isChar(line, i); // next tokens index
                    if (i == -1) // lexical error
                        return false;
                }
            }
        }
        return true;
    }

    public static int isIdentifier(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);
        boolean isSingle = false, valid = true;

        // check if it's a single token identifier
        if (line.charAt(startIndex) == '.' || line.charAt(startIndex) == '+' || line.charAt(startIndex) == '-')
            isSingle = true;

        int i = startIndex+1;
        for (; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == ' ' || isBracket(ch)) // end of token
                break;

            token += ch;
            if (isSingle) {
                if (Character.isDigit(ch) || ch == '.')
                    return isNumber(line, startIndex);
                else
                    valid = false;
            }
            else if (!('a' <= ch && ch <= 'z') && !Character.isDigit(ch) && ch != '.' && ch != '+' && ch != '-')
                valid = false;
        }

        if (valid){
            isReserved(token, startIndex);
            return i-1;
        }
        else {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex+1, token));
            return -1; // lexical error
        }
    }

    /**This function checks if the token is a valid number*/
    public static int isNumber(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);
        boolean hasExp = false, hasDot = false, valid = true;

        // check for binary and hexadecimal number
        if (line.charAt(startIndex) == '0') {
            int result = isHex(line, startIndex);
            if (result != -2)
                return result;
            else {
                result = isBin(line, startIndex);
                if (result != -2)
                    return result;
            }
        }

        // check for decimal and floating-point number
        int i = startIndex + 1;
        for (; i < line.length(); i++) {
            char ch = line.charAt(i);
            token += ch;

            if (ch == ' ') // end of token
                break;
            else if (isBracket(ch)) // end of token
                break;
            else if (ch == '.') {
                if (!hasDot)
                    hasDot = true;
                else
                    valid = false;
            }
            else if (ch == 'e' || ch == 'E') {
                if (!hasExp && Character.isDigit(line.charAt(i-1)))
                    hasExp = true;
                else
                    valid = false;
            }
            else if (ch == '+' || ch == '-') {
                if (line.charAt(i-1) != 'e' && line.charAt(i-1) != 'E')
                    valid = false;
            }
            else if (!Character.isDigit(ch))
                valid = false;
        }
        if (valid) {
            output.add(String.format("NUMBER %d:%d", lineCount, startIndex+1));
            return i-1; // it's a valid token return next tokens starting index
        }
        else  {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex+1, token));
            return -1; // lexical error
        }
    }

    /**This function checks if the token is a valid hexadecimal number*/
    public static int isHex(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);
        boolean valid = true;

        int i;
        if (line.charAt(startIndex+1) == 'x') { // check if it's a hexadecimal number
            i = startIndex + 2;
            for (; i < line.length(); i++) {
                char ch = line.charAt(i);
                token += ch;

                if (ch == ' ') // end of token
                    break;
                else if (isBracket(ch)) // end of token
                    break;
                else if (!Character.isDigit(ch) && !('a' <= ch && ch <= 'f') && !('A' <= ch && ch <= 'F'))
                    valid = false;
            }
        }
        else
            return -2; // not hex

        if (valid) {
            output.add(String.format("NUMBER %d:%d", lineCount, startIndex+1));
            return i-1; // it's a valid token return next tokens starting index
        }
        else {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex+1, token));
            return -1; // lexical error
        }
    }

    /**This function checks if the token is a valid binary number*/
    public static int isBin(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);
        boolean valid = true;

        int i;
        if (line.charAt(startIndex+1) == 'b') { // check if it's a binary number
            i = startIndex + 2;
            for (; i < line.length(); i++) {
                char ch = line.charAt(i);
                token += ch;

                if (ch == ' ') // end of token
                    break;
                else if (isBracket(ch)) // end of token
                    break;
                else if (ch != '1' && ch != '0')
                    valid = false;
            }
        }
        else
            return -2; // not binary

        if (valid) {
            output.add(String.format("NUMBER %d:%d", lineCount, startIndex+1));
            return i-1; // it's a valid token return next tokens starting index
        }
        else {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex+1, token));
            return -1; // lexical error
        }
    }

    /**This function check if the token is a valid character*/
    public static int isChar(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);
        int quoteCount = 1, bsCount = 0;
        boolean hasUni = false, valid = true;

        int i = startIndex + 1;
        for (; i < line.length(); i++) {
            char ch = line.charAt(i);
            token += ch;

            if (ch == ' ') {
                if (line.charAt(i-1) != '\'')
                    valid = false;
                break;
            }
            if (bsCount == 1) {
                if (ch != '\'' && ch != '\\')
                    valid = false;
            }
            if (ch == '\\') {
                bsCount++;
                if (hasUni || bsCount > 2)
                    valid = false;
            }
            else if (ch == '\'') {
                quoteCount++;
                if (bsCount == 0 && quoteCount > 2) // can't have more than 2 quote without backslash -> '\''
                    valid = false;
            }
            else if (Character.isDefined(ch)) { // check if character defined in unicode
                if (bsCount != 0 || hasUni) // can't have both unicode and backslash, or multiple unicodes
                    valid = false;
                hasUni = true;
            }
        }
        if (bsCount == 1 && quoteCount != 3)
            valid = false;

        if (valid) {
            output.add(String.format("CHAR %d:%d", lineCount, startIndex+1));
            return i-1;
        }
        else {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex+1, token));
            return -1;
        }
    }

    public static boolean isBracket(char ch) {
        return ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}';
    }

    public static void isReserved(String token, int startIndex){
        for (int i = 0; i < keywords.length; i++) {
            if (keywords[i].equals(token)) {
                output.add(String.format(("%s %d:%d"), token.toUpperCase(Locale.US), lineCount, startIndex+1));
                return;
            }
        }
        output.add(String.format("IDENTIFIER %d:%d", lineCount, startIndex+1));
    }
}