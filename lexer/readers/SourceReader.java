package lexer.readers;

import java.io.*;

/**
* This class is used to manage the source program input stream;
* each read request will return the next usable character; it
* maintains the source column position of the character
*/
public class SourceReader implements IReader {
  
  private BufferedReader source;
  // line number of source program
  private int lineNumber = 1;
  // position of last character processed
  private int column = -1;
  private StringBuffer currentLine = new StringBuffer(); 
  private boolean completedLine = false;
  private String sourceFile;
  
  /**
  * Construct a new SourceReader
  * 
  * @param sourceFile the String describing the user's source file
  * @exception IOException is thrown if there is an I/O problem
  */
  public SourceReader(String sourceFile) throws IOException {
    this(new BufferedReader(new FileReader(sourceFile)));
    this.sourceFile = sourceFile;
  }
  
  public SourceReader(BufferedReader reader) throws IOException {
    this.source = reader;
  }
  
  public void close() {
    try {
      source.close();
    } catch (Exception e) {
      /* no-op */
    }
  }
  
  private char advance() throws IOException {
    column++;
    
    int i = source.read();
    
    if (i == -1) {
      return '\0';
    }
    currentLine.append((char) i);
    
    return (char) i;
  }
  
  /**
  * read next char; track line #
  * 
  * @return the character just read in
  * @IOException is thrown for IO problems such as end of file
  */
  public char read() {
    try {
      if (completedLine) {
        lineNumber++;
        column = -1;
        completedLine = false;
      }
      
      char character = advance();
      
      if (character == '\r') {
        character = advance();
      }
      
      if (character == '\n') {
        currentLine.delete(0, currentLine.length());
        completedLine = true;
      }
      
      return character;
    } catch (Exception e) {
      return '\0';
    }
  }
  
  public int getColumn() {
    return column;
  }
  
  public int getLineNumber() {
    return lineNumber;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String line;
    try {
      BufferedReader temp = new BufferedReader(new FileReader(sourceFile));
      int i = 1;
      
      
      while ((line = temp.readLine()) != null) {
        if (line.isEmpty()) {
          sb.append(String.format("%3d:%n", i++));
        } else {
          sb.append(String.format("%3d: %s", i++, line));
        }
        if (line != null && !line.isEmpty() && temp.ready()) {
          sb.append(System.lineSeparator());
        }
      } 
      temp.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }
}
