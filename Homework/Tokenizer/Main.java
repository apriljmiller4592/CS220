class JackTokenizer {

    public static void main(String ... jackFilesToTranslate) {
       for (String fileName : jackFilesToTranslate) {
            JackTokenizer jackTokenizer = new JackTokenizer(fileName);
            PrintWriter printWriter;
            try {
                printWriter = new PrintWriter(new FileOutputStream(fileName.replaceAll("jack", "xml")));
                printWriter.println("<tokens>");
                jackTokenizer.getTokens(printWriter);
                printWriter.println("</tokens>");
                printWriter.close();
            } catch (FileNotFoundException e) {
                System.out.println("Cannot write to " + fileName.replaceAll( "jack", "xml"));
            }
       }
    }

    private static final String KEYWORD = "keyword";
    private static final String SYMBOL  = "symbol";
    private static final String INTEGER_CONSTANT = "integerConstant";
    private static final String STRING_CONSTANT  = "stringConstant";
    private static final String IDENTIFIER = "identifier";

    private static final Pattern stringPattern = Pattern.compile("\".*\"");
    private static final Pattern identifierPattern = Pattern.compile("[\\w_]+");
    private static final Pattern integerPattern = Pattern.compile("[0-9]+");
    private static final Pattern symbolPattern = Pattern.compile("[{}()\\[\\].,;+\\-*/&|<>=~]");
    private static final Pattern keywordPattern = Pattern.compile
    ("class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return");
    private static final Pattern bigPattern = Pattern.compile(keywordPattern.pattern() + symbolPattern.pattern() +
    "|" + integerPattern.pattern() + "|" + stringPattern.pattern() + "|" + identifierPattern.pattern());

    private Scanner inputFile;

    /**
     * Full constructor, connects the file with the program.
     * @param fileName The file name that contains Jack code.
     */
    JackTokenizer(String fileName) {
        try {
            inputFile = new Scanner(new FileInputStream(fileName));
        } catch (FileNotFoundException e) {
            System.out.println(fileName + " file not found.\n");
        }
    }

    /**
     * Scans through lines of Jack code, cleans them of comments, breaks the lines into tokens, then translates tokens
     * into XML.
     * @param printWriter The PrintWriter that will be writing the tokens to the .xml file.
     */
    void getTokens(PrintWriter printWriter) {
        for (String token : collectTokens(cleanLines())) {
            printTokens(token, printWriter);
        }
    }

    /**
     * Scans through lines of Jack code, and cleans them of comments/escape characters (\n, \t, etc).
     * @return An ArrayList of lines of Jack code (without comments and escape characters).
     */
    private List <String> cleanLines() {
        List <String> cleanedLines = new ArrayList <> ();
        while (inputFile.hasNextLine()) {
            String cleanInput = inputFile.nextLine().trim();
            cleanInput = cleanInput.replaceAll("//.*", "");
            cleanInput = (cleanInput.startsWith("/*")) ? cleanInput.substring(0, cleanInput.indexOf("/*")) : cleanInput;
            cleanInput = (cleanInput.startsWith("*")) ? cleanInput.substring(0, cleanInput.indexOf("*")) : cleanInput;
            if (!cleanInput.isEmpty()) {
                cleanedLines.add(cleanInput);
            }
        }
        inputFile.close();
        return cleanedLines;
    }

    /**
     * Sorts through lines of Jack code and determines which portions can be tokens.
     * @param jackCodeToBeParsed An ArrayList of cleaned Jack code.
     * @return An ArrayList of tokens derived from the Jack code.
     */
    private List <String> collectTokens(List <String> jackCodeToBeParsed) {
        List <String> tokens = new ArrayList <> ();
        for (String line : jackCodeToBeParsed) {
            Matcher m = bigPattern.matcher(line);
            while (m.find())
                tokens.add(m.group());
        }
        return tokens;
    }

    /**
     * Prints the given token to a .xml file (based on matched pattern).
     * @param token The token given.
     * @param printWriter The PrintWriter that will print the token (in <xml></xml> format) to the .xml file.
     */
    private void printTokens(String token, PrintWriter printWriter) {
        if (token.matches(keywordPattern.pattern())) {
            printWriter.println("<" + KEYWORD + "> " + token + " </" + KEYWORD + ">");
        } else if (token.matches(stringPattern.pattern())) {
            token = token.substring(1);
            printWriter.println("<" + STRING_CONSTANT + "> " + token + " </" + STRING_CONSTANT + ">");
        } else if (token.matches(integerPattern.pattern())) {
            printWriter.println("<" + INTEGER_CONSTANT + "> " + token + " </" + INTEGER_CONSTANT + ">");
        } else if (token.matches(symbolPattern.pattern())) {
            token = switch (token) { // Special characters in XML.
                case "<" -> "&#60";
                case ">" -> "&#62";
                case "&" -> "&#38";
                case "'" -> "&#39";
                case "\"" -> "&#34";
                default -> token;
            };
            printWriter.println("<" + SYMBOL + "> " + token + " </" + SYMBOL + ">");
        } else if (token.matches(identifierPattern.pattern()))
            printWriter.println ("<" + IDENTIFIER + "> " + token + " </" + IDENTIFIER + ">");
    }
}
