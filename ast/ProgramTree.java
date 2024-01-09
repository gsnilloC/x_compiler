package ast;

import visitor.*;

public class ProgramTree extends AST {

  public ProgramTree() {}

  public Object accept(ASTVisitor visitor) {
    return visitor.visitProgramTree(this);
  }
}
