package ast;

import visitor.*;

public class SelectBlockTree extends AST{

    public SelectBlockTree() {}

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitSelectBlockTree(this);
    }
}
