package ast;

import lexer.Symbol;
import lexer.Token;
import visitor.*;

public class IdTree extends AST {

  private Symbol symbol;
  // stack location for codegen
  private int frameOffset = -1;

  /**
   *  @param token - record the symbol from the token Symbol
   */
  public IdTree(Token token) {
    this.symbol = token.getSymbol();
  }

  public Object accept(ASTVisitor visitor) {
    return visitor.visitIdTree(this);
  }

  public Symbol getSymbol() {
    return symbol;
  }

  /**
   *  @param offset is the offset for this variable as determined by the code generator
   */
  public void setFrameOffset(int offset) {
    frameOffset = offset;
  }

  /**
   *  @return the frame offset for this variable - used by codegen
   */
  public int getFrameOffset() {
    return frameOffset;
  }
}
