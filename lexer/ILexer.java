package lexer;

public interface ILexer {
  public Token nextToken();
  
  public Token anonymousIdentifierToken(String string);
}
