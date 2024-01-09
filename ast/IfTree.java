package ast;

import visitor.*;

public class IfTree extends AST {

  public IfTree() {}

  public Object accept(ASTVisitor visitor) {
    return visitor.visitIfTree(this);
  }
}
