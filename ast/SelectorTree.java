package ast;

import visitor.*;

public class SelectorTree extends AST{

    public SelectorTree() {}

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitSelectorTree(this);
    }
}
