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

  private static boolean DEBUG = false;

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

      // RELOP
      // states allocated from 0 to 50
      switch (state) {

        case 0:
          if(c == '<') {
            state = 1;
            lessema += c;
          } else if(c == '=') {
            state = 7;
          } else if(c == '>') {
            state = 3;
            lessema += c;
          } else {
            state = 51;
          }
          break;

        case 1:
          if(c == '=') {
            state = 2;
            lessema += c;
          } else if(c == '>') {
            state = 3;
            lessema += c;
          } else if(c == '-') {
            state = 4;
            lessema += c;
          } else {
            state = 6;
          }
          break;

        case 2:
          lessema += c;
          return new Token("LE");

        case 3:
          lessema += c;
          return new Token("NE");

        case 4:
          if(c == '-') {
            lessema += c;
            state = 5;
          }
          break;

        case 5:
          lessema += c;
          return new Token("ASSIGN");

        case 6:
          retrack();
          return new Token("LT");

        case 7:
          lessema += c;
          return new Token("EQ");

        case 8:
          if(c == '=') {
            lessema += c;
            state = 9;
          } else {
            lessema += c;
            state = 10;
          }

        case 9:
          return new Token("GE");
        
        case 10:
          retrack();
          return new Token("GT");
        }

        // IDs
        // states allocated from 51 to 100
        switch(state){
          case 51:
            if(Character.isLetter(c)){
              state = 52;
              lessema += c;
              if(actualChar == -1){
                return installID(lessema);
              }
            } else {
              state = 101;
            }
            break;
            
          case 52:
            if(Character.isLetterOrDigit(c)){
              state = 52;
              lessema += c;
              // controlla se è finito il file
              if(actualChar == -1) {
                return installID(lessema);
              }
            } else {
              state = 53;
            }
            break;
  
          case 53: 
            state = 101;
            retrack();
            return installID(lessema);
        }

        // NUM
        // states allocated from 101 to 150
        switch(state){

          case 101:
            if(Character.isDigit(c)) {
              state = 102;
              lessema += c;
              if(actualChar== -1){
                return new Token("NUM", lessema);
              } else {
                state = 151;
              }
            }
  
          case 102:
            if(Character.isDigit(c)) {
              state = 102;
              lessema += c;
              if(actualChar== -1){
                return new Token("NUM", lessema);
              } 
            } else if(c == '.') {
              state = 103;
              lessema += c;
            } else if(c == 'E') {
              state = 105;
              lessema += c;
            } else {
              state = 109;
            }
            break;
  
          case 103:
            if(Character.isDigit(c)) {
              state = 104;
              lessema += c;
            }
            break;
  
          case 104:
            if(Character.isDigit(c)) {
              state = 104;
              lessema += c;
              if(actualChar== -1){
                return new Token("NUM", lessema);
              } 
            } else if(c == 'E') {
              state = 105;
              lessema += c;
            } else {
              state = 110;
            }
            break;
  
          case 105:
            if(Character.isDigit(c)) {
              state = 107;
              lessema += c;
            } else if(c == '+' || c == '-') {
              state = 106;
              lessema += c;
            }
            break;
  
          case 106:
            if(Character.isDigit(c)) {
              state = 107;
              lessema += c;
            }
            break;
  
          case 107:
            if(Character.isDigit(c)) {
              state = 107;
              lessema += c;
              if(actualChar== -1){
                return new Token("NUM", lessema);
              }
            } else {
              state = 108;
            }
  
          case 108:
            retrack();
            return new Token("NUM", lessema);
  
          case 109:
            retrack();
            return new Token("NUM", lessema);
  
          case 110:
            retrack();
            return new Token("NUM", lessema);
        }

        // DELIMs
        // states allocated from 151 to 200
        switch (state) {
          case 151:
            if(c == '\n' || c == '\t' || c == ' ') {
              state = 152;
              // lessema += c;
            } else {
              state = 201;
            }
            break;
  
          case 152:
            if(c == '\n' || c == '\t' || c == ' ') {
              state = 152;
              // lessema += c;
            } else {
              state = 201;
            }
            break;
  
          case 153:
            retrack();
            return null;
        }

        // SEP
        // states allocated from 201 to 250
        switch (state) {

          case 201:
            if(c == '(') {
              state = 202;
              lessema += c;
            } else if(c == ')') {
              state = 203;
              lessema += c;
            } else if(c == '{') {
              state = 204;
              lessema += c;
            } else if(c == '}') {
              state = 205;
              lessema += c;
            } else if(c == ',') {
              state = 206;
              lessema += c;
            } else if(c == ';') {
              state = 207;
              lessema += c;
            }
            break;
  
          case 202:
            return new Token("LPAR");
  
          case 203:
            return new Token("RPAR");
  
          case 204:
            return new Token("LBRA");
            
          case 205:
            return new Token("RBRA");
          
          case 206:
            return new Token("COMMA");
  
          case 207:
            return new Token("SEMI");
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
