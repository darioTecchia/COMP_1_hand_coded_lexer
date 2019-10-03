import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class Lexer {

  private File input;
  private RandomAccessFile ramFileReader;
  private static HashMap<String, Token> stringTable; // la struttura dati potrebbe essere una hash map

  private int state;
  private int forward = 0;

  private static boolean DEBUG = true;

  public Lexer() {
    // la symbol table in questo caso la chiamiamo stringTable
    stringTable = new HashMap<String, Token>();
    state = 0;
    // inserimento delle parole chiavi nella stringTable per evitare di scrivere
    // un diagramma di transizione per ciascuna di esse (le parole chiavi
    // verranno "catturate" dal diagramma di transizione e gestite e di
    // conseguenza). IF poteva anche essere associato ad una costante numerica
    stringTable.put("if", new Token("IF"));
    stringTable.put("then", new Token("THEN"));
    stringTable.put("else", new Token("ELSE"));
    stringTable.put("while", new Token("WHILE"));
    stringTable.put("int", new Token("INT"));
  }

  public Boolean initialize(String filePath) {

    // prepara file input per lettura e controlla errori
    this.input = new File(filePath);
    try {
      this.ramFileReader = new RandomAccessFile(this.input, "r");
    } catch (FileNotFoundException e) {
      return false;
    }
    return this.input.exists();
  }

  public int nextChar() throws IOException {
    this.ramFileReader.seek(this.forward++);
    return this.ramFileReader.read();
  }

  public Token nextToken() throws Exception {
		
		//Ad ogni chiamata del lexer (nextToken())
    //si resettano tutte le variabili utilizzate
		this.state = 0;
    
		String lessema = ""; //è il lessema riconosciuto
    char c;
    int actualChar;
		
		while(true){
      
      // legge un carattere da input e lancia eccezione quando incontra EOF per restituire null
      //  per indicare che non ci sono più token
      actualChar = nextChar();
      if(actualChar == -1) {
        throw new EOFException();
      }
      c = (char) actualChar;
      
      if(DEBUG) {
        System.out.println("State: " + state);
        System.out.println("\tLessema: '" + lessema + "'" + "\tchar: '" + c + "'\n");
      }

      // DELs
      switch (state) {
        case 0:
          if(c == '\n' || c == '\t' || c == ' ') {
            state = 1;
            lessema += c;
          } else {
            state = 3;
          }
          break;

        case 1:
          if(c == '\n' || c == '\t' || c == ' ') {
            state = 1;
            lessema += c;
          } else {
            state = 3;
          }
          break;

        case 2:
          state = 3;
          retrack();
          return null;
      }

      // IDs
      switch(state){
				case 3:
					if(Character.isLetter(c)){
						state = 4;
						lessema += c;
            if(actualChar == -1){
							return installID(lessema);
						}
					} else {
            state = 6;
          }
          break;
					
				case 4:
					if(Character.isLetterOrDigit(c)){
            state = 4;
            lessema += c;
            // controlla se è finito il file
						if(actualChar == -1) {
							return installID(lessema);
            }
					} else {
						state = 5;
          }
          break;

        case 5: 
          state = 6;
          retrack();
          return installID(lessema);
      }

      // NUMs
      switch(state){

        case 6:
          if(Character.isDigit(c)) {
            state = 7;
            lessema += c;
            if(actualChar== -1){
							return new Token("NUM", lessema);
						} else {
              state = 16;
            }
          }

        case 7:
          if(Character.isDigit(c)) {
            state = 7;
            lessema += c;
            if(actualChar== -1){
							return new Token("NUM", lessema);
						} 
          } else if(c == '.') {
            state = 8;
            lessema += c;
          } else if(c == 'E') {
            state = 10;
            lessema += c;
          } else {
            state = 14;
          }
          break;

        case 8:
          if(Character.isDigit(c)) {
            state = 9;
            lessema += c;
          }
          break;

        case 9:
          if(Character.isDigit(c)) {
            state = 9;
            lessema += c;
            if(actualChar== -1){
							return new Token("NUM", lessema);
						} 
          } else if(c == 'E') {
            state = 10;
            lessema += c;
          } else {
            state = 15;
          }
          break;

        case 10:
          if(Character.isDigit(c)) {
            state = 12;
            lessema += c;
          } else if(c == '+' || c == '-') {
            state = 11;
            lessema += c;
          }
          break;

        case 11:
          if(Character.isDigit(c)) {
            state = 12;
            lessema += c;
          }
          break;

        case 12:
          if(Character.isDigit(c)) {
            state = 12;
            lessema += c;
            if(actualChar== -1){
							return new Token("NUM", lessema);
						}
          } else {
            state = 13;
          }

        case 13:
          retrack();
          return new Token("NUM", lessema);

        case 14:
          retrack();
          return new Token("NUM", lessema);

        case 15:
          retrack();
          return new Token("NUM", lessema);
      }

      // SEPs
      switch (state) {

        case 16:
          if(c == '(') {
            state = 17;
            lessema += c;
          } else if(c == ')') {
            state = 18;
            lessema += c;
          } else if(c == '{') {
            state = 19;
            lessema += c;
          } else if(c == '}') {
            state = 20;
            lessema += c;
          } else if(c == ',') {
            state = 21;
            lessema += c;
          } else if(c == ';') {
            state = 22;
            lessema += c;
          } else {
            state = 23;
          }
          break;

        case 17:
          return new Token("LPAR");

        case 18:
          return new Token("RPAR");

        case 19:
          return new Token("LBRA");
          
        case 20:
          return new Token("RBRA");
        
        case 21:
          return new Token("COMMA");

        case 22:
          return new Token("SEMI");
      }

      // RELOP
      switch (state) {

        case 23:
          if(c == '<') {
            state = 24;
            lessema += c;
          } else if(c == '=') {
            state = 30;
          } else if(c == '>') {
            state = 31;
            lessema += c;
          }
          break;

        case 24:
          if(c == '=') {
            state = 25;
            lessema += c;
          } else if(c == '>') {
            state = 26;
            lessema += c;
          } else if(c == '-') {
            state = 27;
            lessema += c;
          } else {
            state = 28;
          }
          break;

        case 25:
          lessema += c;
          return new Token("LE");

        case 26:
          lessema += c;
          return new Token("NE");

        case 27:
          if(c == '-') {
            lessema += c;
            state = 27;
          }
          break;

        case 28:
          lessema += c;
          return new Token("ASSIGN");

        case 29:
          retrack();
          return new Token("LT");

        case 30:
          lessema += c;
          return new Token("EQ");

        case 31:
          if(c == '=') {
            lessema += c;
            state = 31;
          } else {
            lessema += c;
            state = 32;
          }

        case 32:
          return new Token("GE");
        
        case 33:
          retrack();
          return new Token("GT");
      }
		}
	}

  private Token installID(String lessema) {
    Token token;

    // utilizzo come chiave della hashmap il lessema
    if (stringTable.containsKey(lessema))
      return stringTable.get(lessema);
    else {
      token = new Token("ID", lessema);
      stringTable.put(lessema, token);
      return token;
    }
  }

  private void retrack() {
    // fa il retract nel file di un carattere
    this.forward-=1;
  }

  private void fail(int nextState, String actualLessema) {
    System.out.println("Error, invalid token: '" + actualLessema + "'");
    this.state = nextState;
  }

}
