import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

/**
 * PPL Project 1
 * 1-	Mustafa Emir Uyar (Representative) 150120007
 * 2-	Ege Keklikçi 150121029
 * 3-	Umut Özil 150121019
 */
public class Main {

    static int lineCount = 1;
    static String[] keywords = {"define", "let", "cond", "if", "begin"}; // reserved keywords
    static ArrayList<String> output = new ArrayList<>();

    public static void main(String[] args) {

        //Taking the input file's path.
        String filePath = "input.txt";

        if (args.length == 0) {
            System.out.print("Enter the filepath: ");
            filePath = new Scanner(System.in).nextLine();
        } else if (!args[0].isEmpty()) {
            filePath = args[0];
        }

        try {
            //Open the file using the filepath
            File inputFile = new File(filePath);
            Scanner input = new Scanner(inputFile);

            // read the input file line by line
            while (input.hasNextLine()) {
                String line = input.nextLine();
                if (line.isEmpty() || line.charAt(0) != '~') // ignore comment lines and empty lines
                    if (!firstLook(line))
                        break;

                lineCount += 1;
            }

            //Create the output file
            File outputFile = new File("output.txt");
            FileWriter writer = new FileWriter(outputFile);

            for (int i = 0; i < output.size(); i++) {
                writer.write(output.get(i));
                writer.write("\n");
            }
            writer.close();

            //Writing the output to the console
            Scanner fileReader = new Scanner(outputFile);
            while (fileReader.hasNextLine())
                System.out.println(fileReader.nextLine());
            fileReader.close();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * This function checks the tokens first index and calls functions accordingly
     */
    public static boolean firstLook(String line) {
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch != ' ') { // ignore whitespaces
                if (ch == '(')
                    output.add(String.format("LEFTPAR %d:%d", lineCount, (i + 1)));
                else if (ch == ')')
                    output.add(String.format("RIGHTPAR %d:%d", lineCount, (i + 1)));
                else if (ch == '[')
                    output.add(String.format("LEFTSQUAREB %d:%d", lineCount, (i + 1)));
                else if (ch == ']')
                    output.add(String.format("RIGHTSQUAREB %d:%d", lineCount, (i + 1)));
                else if (ch == '{')
                    output.add(String.format("LEFTCURLYB %d:%d", lineCount, (i + 1)));
                else if (ch == '}')
                    output.add(String.format("RIGHTCURLYB %d:%d", lineCount, (i + 1)));
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
                // check for string token
                else if (ch == '\"') {
                    i = isString(line, i); // next tokens index
                    if (i == -1) // lexical error
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * This function checks if the current line has identifiers.
     */
    public static int isIdentifier(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);
        boolean isSingle = false, valid = true;

        // check if it's a single token identifier
        if (line.charAt(startIndex) == '.' || line.charAt(startIndex) == '+' || line.charAt(startIndex) == '-')
            isSingle = true;

        int i = startIndex + 1;
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
            } else if (!('a' <= ch && ch <= 'z') && !Character.isDigit(ch) && ch != '.' && ch != '+' && ch != '-')
                valid = false;
        }

        if (valid) {
            isReserved(token, startIndex);
            return i;
        } else {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex + 1, token));
            return -1; // lexical error
        }
    }

    /**
     * This function checks if the token is a valid number
     */
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
            } else if (ch == 'e' || ch == 'E') {
                if (!hasExp && Character.isDigit(line.charAt(i - 1)))
                    hasExp = true;
                else
                    valid = false;
            } else if (ch == '+' || ch == '-') {
                if (line.charAt(i - 1) != 'e' && line.charAt(i - 1) != 'E')
                    valid = false;
            } else if (!Character.isDigit(ch))
                valid = false;
        }
        if (valid) {
            output.add(String.format("NUMBER %d:%d", lineCount, startIndex + 1));
            return i; // it's a valid token return next tokens starting index
        } else {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex + 1, token));
            return -1; // lexical error
        }
    }

    /**
     * This function checks if the token is a valid hexadecimal number
     */
    public static int isHex(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);
        boolean valid = true;

        int i;
        if (line.charAt(startIndex + 1) == 'x') { // check if it's a hexadecimal number
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
        } else
            return -2; // not hex

        if (valid) {
            output.add(String.format("NUMBER %d:%d", lineCount, startIndex + 1));
            return i; // it's a valid token return next tokens starting index
        } else {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex + 1, token));
            return -1; // lexical error
        }
    }

    /**
     * This function checks if the token is a valid binary number
     */
    public static int isBin(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);
        boolean valid = true;

        int i;
        if (line.charAt(startIndex + 1) == 'b') { // check if it's a binary number
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
        } else
            return -2; // not binary

        if (valid) {
            output.add(String.format("NUMBER %d:%d", lineCount, startIndex + 1));
            return i; // it's a valid token return next tokens starting index
        } else {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex + 1, token));
            return -1; // lexical error
        }
    }

    /**
     * This function checks if the token is a valid character
     */
    public static int isChar(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);
        int bsCount = 0;
        boolean hasUni = false, valid = true, escape = false, isExited = false;

        int i = startIndex + 1;
        for (; i < line.length(); i++) {
            char ch = line.charAt(i);
            token += ch;

            if (ch == '\'') {
                if (!escape) { // if ' comes after escape character continue else terminate
                    isExited = true;
                    break;
                } else
                    escape = false;
            } else if (ch == '\\') {
                bsCount++;
                escape = !escape;
                if (hasUni || bsCount > 2) // can't have both unicode and backslash, and more than two backslash
                    valid = false;
            } else if (Character.isDefined(ch)) { // check if character defined in unicode
                hasUni = true;
                if (!hasUni || bsCount != 0) // can't have both unicode and backslash, and more than one unicode
                    valid = false;
            }
        }
        if (valid && isExited) {
            output.add(String.format("CHAR %d:%d", lineCount, startIndex + 1));
            return i;
        } else {
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex + 1, token));
            return -1;
        }
    }

    /**
     * This function checks if character is brackets
     */
    public static boolean isBracket(char ch) {
        return ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}';
    }

    /**
     * This function checks if token is keyword
     */
    public static void isReserved(String token, int startIndex) {
        if (!isBoolean(token, startIndex)) {
            for (int i = 0; i < keywords.length; i++) {
                if (keywords[i].equals(token)) {
                    output.add(String.format(("%s %d:%d"), token.toUpperCase(Locale.US), lineCount, startIndex + 1));
                    return;
                }
            }
            output.add(String.format("IDENTIFIER %d:%d", lineCount, startIndex + 1));
        }

    }

    /**
     * This function checks if the token is a boolean or not
     */
    public static boolean isBoolean(String token, int startIndex) {

        if (token.equals("true") || token.equals("false")) {
            output.add(String.format("BOOLEAN %d:%d", lineCount, startIndex + 1));
            return true;
        }
        return false;
    }

    /**
     * This function recognizes the strings and returns the last index of the string.
     */
    public static int isString(String line, int startIndex) {
        String token = "" + line.charAt(startIndex);

        boolean valid = true;
        boolean previousBackslash = false;
        boolean isExited = false;

        //Check every char at the line
        int i = startIndex + 1;
        for (; i < line.length(); i++) {
            char ch = line.charAt(i);
            token += ch;


            if (ch == '\"') {
                //if the current character is double quote and previous char is not backslash, end the string.
                if (!previousBackslash) {
                    isExited = true;
                    break;
                } else
                    previousBackslash = false;
            } else if (ch == '\\') {
                previousBackslash = !previousBackslash;
            } else if (!Character.isDefined(ch) || previousBackslash) // check if character defined in unicode
                valid = false;

        }

        //return the valid string index and line
        if (valid && isExited) {
            output.add(String.format("STRING %d:%d", lineCount, startIndex + 1));
            return i;
        } else {
            //throw and error if the output is invalid
            output.clear();
            output.add(String.format("LEXICAL ERROR [%d:%d]: Invalid token '%s'", lineCount, startIndex + 1, token));
            return -1;
        }
    }


}