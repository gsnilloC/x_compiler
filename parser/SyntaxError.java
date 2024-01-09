package parser;

import lexer.Token;
import lexer.Tokens;

public class SyntaxError extends Exception {

  private static final long serialVersionUID = 1L;
  private Token tokenFound;
  private Tokens kindExpected;

  /**
   * record the syntax error just encountered
   *
   * @param tokenFound   is the token just found by the parser
   * @param kindExpected is the token we expected to find based on the current
   *                     context
   */
  public SyntaxError(Token tokenFound, Tokens kindExpected) {
    this.tokenFound = tokenFound;
    this.kindExpected = kindExpected;
  }

  void print() {
    System.out.println("Expected: " + kindExpected);
    return;
  }

  @Override
  public String toString() {
    return String.format("Expected [%s], found [%s]", kindExpected, tokenFound);
  }
}