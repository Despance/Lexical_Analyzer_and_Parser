public class Parser {
    final static boolean SUCCESS = true;
    final static boolean FAILURE = false;
    static int depth = 0;
    static String currentToken;
    static String currentLexeme;

    public static void main(String[] args) {


    }


    enum TOKENS {LEFTPAR, RIGHTPAR, LEFTSQUAREB, RIGHTSQUAREB, LEFTCURLYB, RIGHTCURLYB, NUMBER, BOOLEAN, CHAR, STRING, DEFINE, LET, COND, IF, BEGIN, IDENTIFIER}


}
