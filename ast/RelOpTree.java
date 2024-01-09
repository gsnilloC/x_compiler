package ast;

import lexer.Symbol;
import lexer.Token;
import visitor.*;

public class RelOpTree extends AST {

  private Symbol symbol;

  /**
   *  @param token contains the Symbol which indicates the specific relational operator
   */
  public RelOpTree(Token token) {
    this.symbol = token.getSymbol();
  }

  public Object accept(ASTVisitor visitor) {
    return visitor.visitRelOpTree(this);
  }

  public Symbol getSymbol() {
    return symbol;
  }
}
