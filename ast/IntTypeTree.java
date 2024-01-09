package ast;

import visitor.*;

public class IntTypeTree extends AST {

  public IntTypeTree() {}

  public Object accept(ASTVisitor visitor) {
    return visitor.visitIntTypeTree(this);
  }
}
