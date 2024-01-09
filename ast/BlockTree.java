package ast;

import visitor.*;

public class BlockTree extends AST {

  public BlockTree() {}

  public Object accept(ASTVisitor visitor) {
    return visitor.visitBlockTree(this);
  }
}
