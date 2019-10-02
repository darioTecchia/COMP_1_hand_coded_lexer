import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

public class Lexer {

  private File input;
  private FileReader fileReader;
  private BufferedReader bufferedReader;
  private static HashMap<String, Token> stringTable; // la struttura dati potrebbe essere una hash map
  private int state;
  
  private static boolean DEBUG = true;

  public Lexer() {
    // la symbol table in questo caso la chiamiamo stringTable
    stringTable = new HashMap<String, Token>();
    state = 0;
    stringTable.put("if", new Token("IF")); // inserimento delle parole chiavi nella stringTable per evitare di scrivere
                                            // un diagramma di transizione per ciascuna di esse (le parole chiavi
                                            // verranno "catturate" dal diagramma di transizione e gestite e di
                                            // conseguenza). IF poteva anche essere associato ad una costante numerica
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
      this.fileReader = new FileReader(input);
    } catch (FileNotFoundException e) {
      return false;
    }
    this.bufferedReader = new BufferedReader(fileReader);
    return true;
  }

  public Token nextToken() throws Exception {
		
		//Ad ogni chiamata del lexer (nextToken())
    //si resettano tutte le variabili utilizzate
		state = 0;
		String lessema = ""; //è il lessema riconosciuto
    char c;
    int actualChar;
		
		while(true){
      
      // legge un carattere da input e lancia eccezione quando incontra EOF per restituire null
      //  per indicare che non ci sono più token
      actualChar = bufferedReader.read();
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
            state = 2;
          }
          break;

        case 2:
          state = 3;
          retrack();
          break;
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
						break;
					} else {
            state = 6;
            break;
          }
					
				case 4:
					if(Character.isLetterOrDigit(c)){
            state = 4;
            lessema += c;
            // controlla se è finito il file
						if(actualChar == -1) {
							return installID(lessema);
            }
						break;
					} else {
						state = 5;
					}
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
						break;
					} else {
            state = 14;
            break;
          }
          
        case 7:
          if(Character.isDigit(c)){
            state = 7;
            lessema += c;
            // controlla se è finito il file
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
            break;
          } else if(c == '.') {
            state = 8;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
            break;
          } else if(c == 'E') {
            state = 10;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUM", lessema);
            }
          } else {
            state = 13;
            break;
          }
        
        case 8: 
          if(Character.isDigit(c)){
            state = 9;
            lessema += c;
            // controlla se è finito il file
            if(actualChar== -1){
              return new Token("NUMBER", lessema);
            }
            break;
          } else {
            System.out.println("ERROR invalid token: " + lessema);
          }

        case 9:
          if(Character.isDigit(c)) {
            state = 9;
            lessema += c;
            // controlla se è finito il file
            if(actualChar== -1){
              return new Token("NUMBER", lessema);
            }
            break;
          } else if(c == 'E') {
            state = 10;
            lessema += c;
            // controlla se è finito il file
            if(actualChar== -1){
              return new Token("NUMBER", lessema);
            }
            break;
          } else {
            state = 13;
            break;
          }

        case 10:
          if(Character.isDigit(c)) {
            state = 12;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUMBER", lessema);
            }
          } else if(c == '+' || c == '-') {
            state = 11;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUMBER", lessema);
            }
          } else {
            System.out.println("ERROR invalid token: " + lessema);
            break;
          }

        case 11:
          if(Character.isDigit(c)) {
            state = 12;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUMBER", lessema);
            }
          } else {
            System.out.println("ERROR invalid token: " + lessema);
            break;
          }

        case 12:
          if(Character.isDigit(c)) {
            state = 12;
            lessema += c;
            if(actualChar== -1){
              return new Token("NUMBER", lessema);
            }
          } else {
            state = 13;
            break;
          }

        case 13:
          retrack();
          state = 14;
          return new Token("NUMBER", lessema);
      }

      // SEPs
      switch (state) {
        case 14:
          if(c == '(') {
            state = 20;
            return new Token("LPAR");
          }

        case 15:
          if(c == ')') {
            state = 20;
            return new Token("RPAR");
          }

        case 16:
          if(c == '{') {
            state = 20;
            return new Token("LBRACE");
          }

        case 17:
          if(c == '}') {
            state = 20;
            return new Token("RBRACE");
          }

        case 18:
          if(c == ',') {
            state = 20;
            return new Token("COMMA");
          }

        case 19:
          if(c == ';') {
            state = 20;
            return new Token("SEMI");
          }
      }

      // RELOP
      switch (state) {
        case 20:
          if(c == '<') {
            state = 21;
            lessema += c;
          } else {
            System.out.println("ERROR invalid token: " + lessema);
          }
          break;

        case 21:
          if(c == '-') {
            state = 22;
            lessema += c;
          } else {
            System.out.println("ERROR invalid token: " + lessema);
          }
          break;

        case 22:
          if(c == '-') {
            state = 23;
            lessema += c;
          } else {
            System.out.println("ERROR invalid token: " + lessema);
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
    
  }

}
