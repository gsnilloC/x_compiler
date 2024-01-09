package ast;

import lexer.Symbol;
import lexer.Token;
import visitor.*;


public class HexTree extends AST{

    private Symbol symbol;

    public HexTree(Token token){
        this.symbol = token.getSymbol();
    }

    @Override
    public Object accept (ASTVisitor visitor){
        return visitor.visitHexTree(this);
    }

    public Symbol getSymbol() {
        return symbol;
    }    
}
