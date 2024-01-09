package ast;

import lexer.Symbol;
import lexer.Token;
import visitor.*;

public class AddOpTree extends AST {

  private Symbol symbol;

  public AddOpTree(Token token) {
    this.symbol = token.getSymbol();
  }

  public Object accept(ASTVisitor visitor) {
    return visitor.visitAddOpTree(this);
  }

  public Symbol getSymbol() {
    return symbol;
  }
}
