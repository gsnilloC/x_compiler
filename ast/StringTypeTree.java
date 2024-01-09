package ast;

import visitor.*;

public class StringTypeTree extends AST{
    
    public StringTypeTree() {}
    
    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitStringTypeTree(this);
    }
}
