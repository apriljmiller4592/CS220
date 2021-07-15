import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {

    public static final String VALID_SYMBOLS = "[]{}(){}.,;+-*/&|<>=~ ";
    private static String cleanInput;
    private static Scanner inputFile;
    private static char symbol;
    private static String currentToken;


    public JackTokenizer (String fileName) {
        try {
            inputFile = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("Error loading " + e.getMessage());
        }
        symbol = '\0';
        cleanInput = "";
    }

    public static boolean hasMoreTokens() {
        return inputFile.hasNext();
    }


    public static void advance() {
        cleanInput = inputFile.nextLine();
        cleanInput = getCleanLine(cleanInput);
    }


    private static String getCleanLine(String cleanInput) {
        if (cleanInput.contains("//")) {
            cleanInput = cleanInput.substring(0, cleanInput.indexOf("//"));
        }
        else if (cleanInput.contains("/*")) {
            cleanInput = cleanInput.substring(0, cleanInput.indexOf("/*"));
        }
        else if (cleanInput.contains("/**")) {
            cleanInput = cleanInput.substring(0, cleanInput.indexOf("/**"));
        }
        else if (cleanInput.contains("*")) {
            cleanInput = cleanInput.substring(0, cleanInput.indexOf("*"));
        }
        return cleanInput.trim();
    }


    public static TokenType getTokenType() {
        if (isKeyword()) {
            return TokenType.KEYWORD;
        }
        else if (isSymbol()) {
            symbol = getSymbol();
            return TokenType.SYMBOL;
        }
        else if (isIntConstant()) {
            return TokenType.INT_CONST;
        }
        else if (isStringConst()) {
            return TokenType.STRING_CONST;
        }
        else if (isIdentifier()) {
            return TokenType.IDENTIFIER;
        }
        return null;
    }

    public static char getSymbol() {
        symbol = currentToken.charAt(0);
        return symbol;
    }

    public static String getStringVal() {
        return currentToken.substring(currentToken.indexOf('"') + 1);
    }

    private static boolean isKeyword() {
        for (Keyword key : Keyword.values()) {
            if (currentToken.equals(key.toString().toLowerCase())) {
                return true;
            }
        }
        for (Kind kind : Kind.values()) {
            if (currentToken.equals(kind.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }


    private static boolean isSymbol() {
        return VALID_SYMBOLS.contains(currentToken);
    }


    private static boolean isIdentifier() {
        return !Character.isDigit(currentToken.charAt(0));
    }

    private static boolean isStringConst() {
        String regex = "^[a-zA-Z0-9_]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(currentToken.substring(currentToken.indexOf('"') + 1));
        if (currentToken.charAt(0) == '"') {
            return matcher.matches();
        }
        return false;
    }

    private static boolean isIntConstant() {
        return Character.isDigit(currentToken.charAt(0));
    }

    private static void printElements(String tag, String value, PrintWriter outputFile) {
        outputFile.println("<" + tag + "> " + value + " </" + tag + ">");
    }

    public static void main(String[] args) {

        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();

        String jackFileName, xmlFileName;
        Scanner keyboard = new Scanner(System.in);
        System.out.print("Enter Jack file name (.jack): ");


        jackFileName = keyboard.nextLine();

        keyboard.close();

        xmlFileName = jackFileName.replace("jack", "xml");

        JackTokenizer jackTokenizer = new JackTokenizer(jackFileName);

        try {
            PrintWriter printWriter = new PrintWriter(new File(xmlFileName));
            printWriter.println("<tokens>");
            while (hasMoreTokens()) {
                advance();
                for (int i = 0; i < cleanInput.length(); i++) {
                    char c = cleanInput.charAt(i);
                    if (c == ' ') {
                        if (token.length() > 0) {
                            tokens.add(token.toString());
                        }
                        token = new StringBuilder();
                    }
                    else if (VALID_SYMBOLS.contains(String.valueOf(c))) {
                        if (token.length() > 0) {
                            tokens.add(token.toString());
                        }
                        tokens.add(String.valueOf(c));
                        token = new StringBuilder();
                    }
                    else {
                        token.append(c);
                    }
                }
            }

            int tokenStringCounter = 0;
            StringBuilder stringBuilder = new StringBuilder();

            String tag;

            for (int i = 0; i < tokens.size(); i++) {

                currentToken = tokens.get(i);

                if (!isStringConst() && tokenStringCounter == 0) {

                    if (getTokenType().equals(TokenType.INT_CONST)) {

                        tag = "integerConstant";
                    }
                    else if (getTokenType().equals(TokenType.SYMBOL)) {

                        tag = getTokenType().toString().toLowerCase();

                        if (getSymbol() == '<') {
                            tokens.set(i, "&lt;");
                        }
                        else if (symbol == '>') {
                            tokens.set(i, "&gt;");
                        }
                        else if (symbol == '"') {
                            tokens.set(i, "&quot;");
                        }
                        else if (symbol == '&') {
                            tokens.set(i, "&amp;");
                        }
                    }
                    else {
                        tag = getTokenType().toString().toLowerCase();
                    }

                    printElements(tag, tokens.get(i), printWriter);
                }

                else {
                    if (!getStringVal().isEmpty()) {
                        stringBuilder.append(getStringVal()).append(" ");
                    }

                    if (isStringConst() && tokenStringCounter > 0) {

                        tag = "stringConstant";

                        printElements(tag, stringBuilder.toString(), printWriter);

                        tokenStringCounter = 0;
                        stringBuilder = new StringBuilder();
                    }
                    else {
                        tokenStringCounter++;
                    }
                }
            }
            printWriter.println("</tokens>");
            printWriter.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error loading " + e.getMessage());
            e.printStackTrace();
        }
    }
}