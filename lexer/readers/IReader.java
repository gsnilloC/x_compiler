package lexer.readers;

public interface IReader {
  public char read();

  public int getColumn();

  public int getLineNumber();

  public void close();
}
