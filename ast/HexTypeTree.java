package ast;

import visitor.ASTVisitor;

public class HexTypeTree extends AST{

    public HexTypeTree() {}

    @Override
    public Object accept(ASTVisitor visitor) {
       return visitor.visitHexTypeTree(this);
    }
}
