import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Lexer {

  private File input;
  private int forward;
  private int state;
  private static Map<String, Token> symbolTable;
  private RandomAccessFile raf;

  public Lexer() {
    forward = 0;
    state = 0;
    symbolTable = new HashMap<String, Token>();
    symbolTable.put("if", new Token("IF"));
    symbolTable.put("then", new Token("THEN"));
    symbolTable.put("else", new Token("ELSE"));
    symbolTable.put("while", new Token("WHILE"));
    symbolTable.put("int", new Token("INT"));
    symbolTable.put("float", new Token("FLOAT"));
  }

  // prepara file input per lettura e controlla errori
  public boolean initialize(String filePath) throws IOException {

    input = new File(filePath);
    raf = new RandomAccessFile(input, "r");
    return input.exists();
  }

  public int nextChar() throws IOException {
    raf.seek(forward++);
    return raf.read();
  }

  public Token nextToken() throws Exception {
    state = 0;
    String lessema = "";
    // legge un carattere da input e lancia eccezione quando incontra EOF per
    // restituire null
    // per indicare che non ci sono più token
    int value;
    char c;
    while (true) {

      value = nextChar();
      c = (char) value;
      // relop
      switch (state) {
      case 0:
        if (Character.isDigit(c)) {
          state = 15;
        } else if (Character.isLetter(c)) {
          state = 12;
        } else if (Character.isSpaceChar(c) || c == '\n' || c == '\t') {
          state = 23;
        } else if (c == '(' || c == ')' || c == '{' || c == '}' || c == ')' || c == ',' || c == ';') {
          state = 24;
        } else if (c == '<') {
          state = 1;
        } else if (c == '=') {
          /* state=5 */
          return new Token("RELOP", "EQ");
        } else if (c == '>') {
          state = 6;
        } else if (value == -1) {
          return null;
        } else {
          return new Token("ERROR", String.valueOf(c));
        }
        break;

      case 1:
        if (c == '=') {
          /* state=2 */
          return new Token("RELOP", "LE");
        } else if (c == '>') {
          /* state=3 */
          return new Token("RELOP", "NE");
        } else if (c == '-') {
          state = 9;
        } else {
          retract();
          return new Token("RELOP", "LT");
        }
        break;

      case 6:
        if (c == '=')
          /* state=7 */
          return new Token("RELOP", "GE");
        else /* state=8; */ {
          retract();
          return new Token("RELOP", "GT");
        }

      case 9:
        if (c == '-') {
          return new Token("RELOP", "ASSIGN");
        } else {
          retract();
          retract();
          return new Token("RELOP", "LT");
        }

      }

      // switch ID
      switch (state) {

      case 12:
        if (Character.isLetter(c)) {
          state = 13;
          lessema += c;
          // Nel caso in cui il file è terminato ma ho letto qualcosa di valido
          // devo lanciare il token (altrimenti perderei l'ultimo token, troncato per
          // l'EOF)
          if (value == -1) {
            return installID(lessema);
          }
        } else
          state = 15;
        break;

      case 13:
        if (Character.isLetterOrDigit(c)) {
          lessema += c;
          if (value == -1)
            return installID(lessema);
          break;
        } else {
          retract();
          return installID(lessema);
        }

      }

      // switc number
      switch (state) {
      case 15:
        if (Character.isDigit(c)) {
          state = 16;
          lessema += c;
        } else {
          state = 23;
        }
        break;

      case 16:
        if (Character.isDigit(c)) {
          state = 16;
          lessema += c;
        } else if (c == '.') {
          state = 17;
          lessema += c;
        } else if (value == -1) {
          state = 23;
          return new Token("NUM", lessema);
        } else if (c == 'E') {
          state = 19;
          lessema += c;
        } else {
          retract();
          return new Token("NUM", lessema);
        }
        break;

      case 17:
        if (Character.isDigit(c)) {
          state = 18;
          lessema += c;
        } else if (value == -1)
          return new Token("NUM", lessema);
        else {
          retract();
          return new Token("NUM", lessema);
        }
        break;

      case 18:
        if (Character.isDigit(c)) {
          state = 18;
          lessema += c;
        } else if (value == -1)
          return new Token("NUM", lessema);
        else if (c == 'E') {
          state = 19;
          lessema += c;
        } else {
          retract();
          return new Token("NUM", lessema);
        }
        break;

      case 19:
        if (c == '+' || c == '-') {
          state = 20;
          lessema += c;
        } else if (Character.isDigit(c)) {
          state = 21;
          lessema += c;
        } else
          state = 23;
        break;

      case 20:
        if (Character.isDigit(c)) {
          state = 21;
          lessema += c;
        }
        break;

      case 21:
        if (Character.isDigit(c)) {
          state = 21;
          lessema += c;
        } else {
          retract();
          return new Token("NUM", lessema);
        }
        break;

      }

      // switch Delimitatori
      switch (state) {
      case 23:
        if (Character.isSpaceChar(c) || c == '\n' || c == '\t')
          state = 23;
        else
          state = 24;
        break;
      }

      // switch Separatori
      switch (state) {

      case 24:
        if (c == '(')
          return new Token("LPAR");
        else if (c == ')')
          return new Token("RPAR");
        else if (c == '{')
          return new Token("LBRA");
        else if (c == '}')
          return new Token("RBRA");
        else if (c == ',')
          return new Token("COMMA");
        else if (c == ';')
          return new Token("SEMI");
        else {
          retract();
          state = 0;
        }

      }

    }
  }

  private Token installID(String lessema) {
    Token token;
    // utilizzo come chiave della hashmap il lessema
    if (symbolTable.containsKey(lessema))
      return symbolTable.get(lessema);
    else {
      token = new Token("ID", lessema);
      symbolTable.put(lessema, token);
      return token;
    }
  }

  private void retract() {
    forward--;
  }

}