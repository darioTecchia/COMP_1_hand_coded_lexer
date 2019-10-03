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
        System.out.println("\tLessema: '" + lessema + "'" + "\tchar: '" + c + "'");
      }

      // DELs
      switch (state) {
        case 0:
          if(c == '\n' || c == '\t' || c == ' ') {
            state = 2;
            // lessema += c;
          } else {
            state = 3;
          }
          break;

        case 1:
          if(c == '\n' || c == '\t' || c == ' ') {
            state = 1;
            // lessema += c;
          } else {
            state = 3;
          }
          break;

        case 2:
          state = 3;
          // retrack();
          return null;
          // break;
      }

      // IDs
      switch(state){
				case 3:
					if(Character.isLetter(c)){
						state = 4;
						lessema += c;
						// Nel caso in cui il file è terminato ma ho letto qualcosa di valido
						// devo lanciare il token (altrimenti perderei l'ultimo token, troncato per l'EOF) 
            // controlla se è finito il file
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
					if(Character.isDigit(c)){
						state = 7;
            lessema += c;
            // controlla se è finito il file
						if(actualChar== -1){
							return new Token("NUM", lessema);
						}
					} else {
            state = 14;
          }
          break;
          
        case 7:
          if(Character.isDigit(c)){
            state = 7;
            lessema += c;
            // controlla se è finito il file
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else if(c == '.') {
            state = 8;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else if(c == 'E') {
            state = 10;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else {
            state = 13;
          }
          break;
        
        case 8: 
          if(Character.isDigit(c)){
            state = 9;
            lessema += c;
            // controlla se è finito il file
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else {
            fail(14, lessema);
          }
          break;

        case 9:
          if(Character.isDigit(c)) {
            state = 9;
            lessema += c;
            // controlla se è finito il file
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else if(c == 'E') {
            state = 10;
            lessema += c;
            // controlla se è finito il file
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else {
            state = 13;
          }
          break;

        case 10:
          if(Character.isDigit(c)) {
            state = 12;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else if(c == '+' || c == '-') {
            state = 11;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else {
            fail(14, lessema);
          }
          break;

        case 11:
          if(Character.isDigit(c)) {
            state = 12;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else {
            fail(14, lessema);
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
          break;

        case 13:
          retrack();
          state = 14;
          return new Token("NUM", lessema);
      }

      // SEPs
      switch (state) {
        case 14:
          if(c == '(') {
            state = 20;
            return new Token("LPAR");
          } else {
            state = 20;
          }
          break;

        case 15:
          if(c == ')') {
            state = 20;
            return new Token("RPAR");
          } else {
            state = 20;
          }
          break;

        case 16:
          if(c == '{') {
            state = 20;
            return new Token("LBRACE");
          } else {
            state = 20;
          }
          break;

        case 17:
          if(c == '}') {
            state = 20;
            return new Token("RBRACE");
          } else {
            state = 20;
          }
          break;

        case 18:
          if(c == ',') {
            state = 20;
            return new Token("COMMA");
          } else {
            state = 20;
          }
          break;

        case 19:
          if(c == ';') {
            state = 20;
            return new Token("SEMI");
          } else {
            state = 20;
          }
          break;
      }

      // RELOP
      switch (state) {
        case 20:
          if(c == '<') {
            state = 21;
            lessema += c;
          }
          break;

        case 21:
          if(c == '-') {
            state = 22;
            lessema += c;
          }
          break;

        case 22:
          if(c == '-') {
            state = 23;
            lessema += c;
          }
          break;

        case 23:
          retrack();
          return new Token("ASSIGN");
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
    System.out.println("Error: " + actualLessema);
    this.state = nextState;
  }

}
