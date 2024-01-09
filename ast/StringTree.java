package ast;

import visitor.*;
import lexer.Symbol;
import lexer.Token;

public class StringTree extends AST{

    private Symbol symbol;

    public StringTree(Token token){
        this.symbol = token.getSymbol();
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visitStringTree(this);
    }

    public Symbol getSymbol() {
        return symbol;
    }
    
}
