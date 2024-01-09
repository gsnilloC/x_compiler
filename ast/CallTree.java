package ast;

import visitor.*;

public class CallTree extends AST {

  public CallTree() {}

  public Object accept(ASTVisitor visitor) {
    return visitor.visitCallTree(this);
  }
}
