package ast;

import visitor.ASTVisitor;

public class UnlessTree extends AST{

    public UnlessTree() {}

    @Override
    public Object accept(ASTVisitor visitor) {
       return visitor.visitUnlessTree(this);
    }    
}
