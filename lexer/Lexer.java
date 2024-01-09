package lexer;

import java.io.BufferedReader;
import java.io.FileReader;

import lexer.readers.IReader;
import lexer.readers.SourceReader;

/**
 * The Lexer class is responsible for scanning the source file
 * which is a stream of characters and returning a stream of
 * tokens; each token object will contain the string (or access
 * to the string) that describes the token along with an
 * indication of its location in the source program to be used
 * for error reporting; we are tracking line numbers; white spaces
 * are space, tab, newlines
 */
public class Lexer implements ILexer {

  // next character to process
  private char ch;
  public IReader source;

  // positions in line of current token
  private int startPosition, endPosition, lineNo;

  /**
   * Lexer constructor
   * 
   * @param sourceFile is the name of the File to read the program source from
   */
  public Lexer(String sourceFile) throws Exception {
    this(new SourceReader(sourceFile));
  }

  public Lexer(IReader reader) throws Exception {
    TokenType.init();
    this.source = reader;
    nextChar();
  }

  public Token newToken(String tokenString, int start, int end, Tokens type, int lineNo) {
    return new Token(start, end, Symbol.symbol(tokenString, type), lineNo);
  }

  private void nextChar() {
    ch = source.read();
    endPosition++;
  }

  private void scanPastWhitespace() {
    while (Character.isWhitespace(ch) && !eofReached()) {
      nextChar();
    }
  }

  private Token reservedWordOrIdentifier() {
    String identifier = "";

    do {
      identifier += ch;
      nextChar();
    } while (Character.isJavaIdentifierPart(ch) && !eofReached());

    return newToken(
        identifier,
        startPosition,
        endPosition - 1,
        Tokens.Identifier,
        lineNo);
  }

  private Token integer() {
    String number = "";

    // if a "0" is followed by "x" or "X" then the number is a hexadecimal
    /* */
    if (ch == '0') {
      nextChar();
      if (ch == 'x' || ch == 'X') {
        if (ch == 'x') {
          number = number + "0x";
          nextChar();
        } else if (ch == 'X') {
          number = number + "0X";
          nextChar();
        }
        // Check if the hexadecimal has any digits
        if (Character.toString(ch).matches("[0-9A-Fa-f]")) {
          // will use this variable to keep track of the length of the hexadecimal
          int hexCheck = 0;
          do {
            // check if the current character is a valid hexadecimal digit
            if (!Character.toString(ch).matches("[0-9A-Fa-f]")) {
              return error(Character.toString(ch));
            }
            number += ch;
            nextChar();
            hexCheck++;
          } while (hexCheck < 6 && !eofReached());
        } else {
          return error(Character.toString(ch));
        }
        return newToken(number, startPosition, endPosition - 1, Tokens.HexLit, lineNo);
      } else if (!Character.isDigit(ch)) {
        // if the 0 is not followed by a digit or x/X, it is a valid number '0'
        return newToken("0", startPosition - 1, endPosition - 1, Tokens.INTeger, lineNo);
      }
    }

    do {
      number += ch;
      nextChar();
    } while (Character.isDigit(ch) && !eofReached());
    return newToken(number, startPosition, endPosition - 1, Tokens.INTeger, lineNo);
}


  /**
   * Prints out an error string and returns the EOF token to halt lexing
   */
  private Token error(String errorString) {
    System.err.println(
        String.format("******** illegal character: %s", errorString));

    return newToken(null, startPosition, endPosition, Tokens.EOF, lineNo);
  }

  private void ignoreComment() {
    int oldLine = source.getLineNumber();

    do {
      nextChar();
    } while (oldLine == source.getLineNumber() && !eofReached());
  }

  private boolean eofReached() {
    return ch == '\0' ;
  }

  private Token singleCharacterOperatorOrSeparator(String character) {
    Symbol symbol = Symbol.symbol(character, Tokens.BogusToken);

    // If symbol is still null, we did not find an operator in the symbol table,
    // and did not encounter the end of file, so this is an error
    if (symbol == null) {
      return error(character);
    } else {
      return newToken(
          character,
          startPosition,
          // -1 since we got next character to test for 2 char operators
          endPosition - 1,
          symbol.getKind(),
          lineNo);
    }
  }

  private Token operatorOrSeparator() {
    String singleCharacter = "" + ch;

    if (eofReached()) {
      return newToken(singleCharacter, startPosition, endPosition, Tokens.EOF, lineNo);
    }

    // We might have a two character operator, so we need to test for that first
    // by looking ahead one character.
    nextChar();

    String doubleCharacter = singleCharacter + ch;
    Symbol symbol = Symbol.symbol(doubleCharacter, Tokens.BogusToken);

    if (symbol == null) {
      // A two character operator was not found in the symbol table,
      // so this must be a single character operator (or invalid)
      return singleCharacterOperatorOrSeparator(singleCharacter);
    } else if (symbol.getKind() == Tokens.Comment) {
      ignoreComment();
      return nextToken();
    } else {
      // We have a valid, two character operator (advance past second char)
      nextChar();

      return newToken(
          doubleCharacter,
          startPosition,
          endPosition - 1,
          symbol.getKind(),
          lineNo);
    }
  }

  /**
   * @return the next Token found in the source file
   */
  public Token nextToken() {
    scanPastWhitespace();

    startPosition = source.getColumn();
    endPosition = startPosition;

    if (Character.isJavaIdentifierStart(ch)) {
      return reservedWordOrIdentifier();
    }

    if (Character.isDigit(ch)) {
      return integer();
    }
    // when "@" is encountered start creating new string and append characters until
    // corresponding "@" is found or EOF
    if (ch == '@') {
      StringBuilder newString = new StringBuilder();
      newString.append(ch);
      nextChar();

      while (ch != '@' && !eofReached()) {
        newString.append(ch);
        nextChar();
        endPosition++;
      }

      if (eofReached()) {
        return error("EOF");
      }
      newString.append(ch);
      nextChar();

      return newToken(newString.toString(), startPosition, (endPosition / 2) + 1, Tokens.StringLit,
          lineNo);
    }

    if (Symbol.symbol(Character.toString(ch), Tokens.BogusToken) != null) {
      return operatorOrSeparator();
    }

    if (ch != '@' && !eofReached()) {
      return error(Character.toString(ch));
    }

    return operatorOrSeparator();
  }

  /**
   * Used by the constrainer to build intrinsic trees
   */
  public Token anonymousIdentifierToken(String identifier) {
    return newToken(identifier, -1, -1, Tokens.Identifier, lineNo);
  }

  @Override
public String toString() {
    return source.toString();
}

  public static void main(String args[]) {
    // takes argument from command line, if empty return usage instruction
    if (args.length == 0) {
      System.out.println("usage: java lexer.Lexer filename.x");
      return;
    }

    String fileName = args[0] ;
    
    try {
      Lexer lex = new Lexer(fileName);
      Token token = lex.nextToken();

      while (token.getKind() != Tokens.EOF) {
        String p = String.format(
            "%-11s left: %-8d right: %-8d line: ",
            token.getLexeme(),
            token.getLeftPosition(),
            token.getRightPosition());

        System.out.println(p + " " + lex.source.getLineNumber() + "        " + token.getKind());

        token = lex.nextToken();
      }

      if (token.getKind() == Tokens.EOF) {
        System.out.println(" ");
      }

      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      int lineNo = 1;
      String line;

      while ((line = reader.readLine()) != null) {
        System.out.println(String.format("%5d: %s", lineNo, line));
        lineNo++;
      }
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}