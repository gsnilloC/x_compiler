package parser;

import ast.*;
import java.util.*;
import lexer.*;

/**
* The Parser class performs recursive-descent parsing; as a by-product it will
* build the <b>Abstract Syntax Tree</b> representation for the source
* program.
* 
* Following is the Grammar we are using:
*
* Program:
* PROGRAM -> 'program' BLOCK
*
* Blocks:
* BLOCK -> '{' D* S* '}'
*
* Variable Declaration:
* D -> TYPE NAME
* 
* Function Declaration:
* D -> TYPE NAME FUNHEAD BLOCK
*
* Types:
* TYPE -> 'int'
* TYPE -> 'boolean'
* TYPE -> 'string'
* TYPE -> 'hex'
*
* Formal Parameter List:
* FUNCHEAD -> '(' (D list ',')? ')'
*
* Statements:
* S -> 'if' E 'then' BLOCK 'else' BLOCK
* S -> 'while' E BLOCK
* S -> 'return' E
* S -> BLOCK
* S -> NAME '=' E
* S -> 'if' E 'then' BLOCK
* S -> 'unless' E 'then' BLOCK
* S -> ‘select’ NAME SELECT_BLOCK
*
* Expressions (Lowest Priority):
* E -> SE
* E -> SE '==' SE
* E -> SE '!=' SE
* E -> SE '<' SE
* E -> SE '<=' SE
* E -> SE '>' SE
* E -> SE '>=' SE
*
* Simple Expressions (Addition Operators/Second Lowest Priority):
* SE -> T
* SE -> SE '+' T
* SE -> SE '-' T
* SE -> SE '|' T
*
* Terms (Multiplication Operators/Highest Priority):
* T -> F
* T -> T '*' F
* T -> T '/' F
* T -> T '&' F
*
* Factors (Parenthesized expressions, identifiers, literals, function calls):
* F -> '(' E ')'
* F -> NAME
* F -> <int>
* F -> NAME '(' (E list ',')? ')'
* F -> <string>
* F -> <hex>
*
* Identifier:
* NAME -> <id>
*
* Select:
* SELECT_BLOCK -> '{'' SELECTOR+ '}'
* SELECTOR -> '[' E '] '->' BLOCK
*/

public class Parser {
  
  private Token currentToken;
  private ILexer lex;
  private EnumSet<Tokens> relationalOps = EnumSet.of(
  Tokens.Equal,
  Tokens.NotEqual,
  Tokens.Less,
  Tokens.LessEqual,
  Tokens.Greater,
  Tokens.GreaterEqual);
  private EnumSet<Tokens> addingOps = EnumSet.of(
  Tokens.Plus,
  Tokens.Minus,
  Tokens.Or);
  private EnumSet<Tokens> multiplyingOps = EnumSet.of(
  Tokens.Multiply,
  Tokens.Divide,
  Tokens.And,
  Tokens.Modulo);
  
  /**
  * Construct a new Parser;
  *
  * @param sourceProgram - source file name
  * @exception Exception - thrown for any problems at startup (e.g. I/O)
  */
  public Parser(String sourceProgram) throws Exception {
    try {
      lex = new Lexer(sourceProgram);
      scan();
    } catch (Exception e) {
      System.out.println("********exception*******" + e.toString());
      throw e;
    }
  }
  
  // Constructor used for testing
  public Parser(ILexer lexer) throws Exception {
    new TokenType();
    lex = lexer;
    scan();
  }
  
  public ILexer getLex() {
    return (ILexer) lex;
  }
  
  /**
  * Execute the parse command
  *
  * @return the AST for the source program
  * @exception Exception - pass on any type of exception raised
  */
  public AST execute() throws Exception {
    try {
      return rProgram();
    } catch (SyntaxError e) {
      e.print();
      throw e;
    }
  }
  
  /**
  * Program:
  * PROGRAM -> 'program' BLOCK
  *
  * @return the program tree
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rProgram() throws SyntaxError {
    // note that rProgram actually returns a ProgramTree; we use the
    // principle of substitutability to indicate it returns an AST
    AST t = new ProgramTree();
    expect(Tokens.Program);
    t.addKid(rBlock());
    return t;
  }
  
  /**
  * Blocks:
  * BLOCK -> '{' D* S* '}'
  *
  * @return block tree
  * @exception SyntaxError - thrown for any syntax error e.g. an expected
  *                        left brace isn't found
  */
  public AST rBlock() throws SyntaxError {
    expect(Tokens.LeftBrace);
    AST t = new BlockTree();
    
    // Get declarations until there are no more matches for declarations
    while (startingDecl()) {
      t.addKid(rDecl());
    }
    
    // Get statements until there are no more matches for statements
    while (startingStatement()) {
      t.addKid(rStatement());
    }
    
    expect(Tokens.RightBrace);
    
    return t;
  }
  
  boolean startingDecl() {
    return isNextTok(Tokens.Int) || isNextTok(Tokens.BOOLean)
    || isNextTok(Tokens.StringType) || isNextTok(Tokens.HexType);
  } 
  
  boolean startingStatement() {
    return (isNextTok(Tokens.If) ||
    isNextTok(Tokens.While) ||
    isNextTok(Tokens.Return) ||
    isNextTok(Tokens.LeftBrace) ||
    isNextTok(Tokens.Unless) ||
    isNextTok(Tokens.Select) ||
    isNextTok(Tokens.Identifier));
  }
  
  /**
  * Variable Declaration:
  * D -> TYPE NAME
  * 
  * Function Declaration:
  * D -> TYPE NAME FUNHEAD BLOCK
  *
  * @return either the decl tree or the functionDecl tree
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rDecl() throws SyntaxError {
    AST t, t1;
    t = rType();
    t1 = rName();
    
    // A LeftParen indicates that this must be a function
    // (this is the beginning of the formal parameters list)
    if (isNextTok(Tokens.LeftParen)) {
      t = (new FunctionDeclTree()).addKid(t).addKid(t1);
      t.addKid(rFuncHead());
      t.addKid(rBlock());
      return t;
    }
    t = (new DeclTree()).addKid(t).addKid(t1);
    
    return t;
  }
  
  /**
  * Types:
  * TYPE -> 'int'
  * TYPE -> 'boolean'
  * TYPE -> 'string'
  * TYPE -> 'hex'
  *
  * @return either the intType or boolType tree
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rType() throws SyntaxError {
    AST t;
    
    if (isNextTok(Tokens.Int)) {
      t = new IntTypeTree();
      scan();
    } else if (isNextTok(Tokens.BOOLean)) {
      t = new BoolTypeTree();
      scan();
    } else if (isNextTok(Tokens.StringType)) {
      t = new StringTypeTree();
      scan();
    } else {
      t = new HexTypeTree();
      scan();
    }
    return t;
  }
  
  /**
  * Formal Parameter List:
  * FUNCHEAD -> '(' (D list ',')? ')'
  *
  * note a funchead is a list of zero or more decl's,
  * separated by commas, all in parens
  *
  * @return the formals tree describing this list of formals
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rFuncHead() throws SyntaxError {
    AST t = new FormalsTree();
    expect(Tokens.LeftParen);
    
    if (!isNextTok(Tokens.RightParen)) {
      do {
        t.addKid(rDecl());
        if (isNextTok(Tokens.Comma)) {
          scan();
        } else {
          break;
        }
      } while (true);
    }
    
    expect(Tokens.RightParen);
    
    return t;
  }
  
  /**
  * Statements:
  * S -> 'if' E 'then' BLOCK
  * S -> 'if' E 'then' BLOCK 'else' BLOCK
  * S -> 'while' E BLOCK
  * S -> 'return' E
  * S -> BLOCK
  * S -> NAME '=' E
  * S -> 'unless' E SELECT_BLOCK
  * S -> 'select' NAME 'then' BLOCK
  *
  * @return the tree corresponding to the statement found
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rStatement() throws SyntaxError {
    AST t;
    
    if (isNextTok(Tokens.If)) {
      scan();
      t = new IfTree();
      
      t.addKid(rExpr());
      
      expect(Tokens.Then);
      t.addKid(rBlock());
      
      if (isNextTok(Tokens.Else)){               
        expect(Tokens.Else);
        t.addKid(rBlock());
      }
      return t;
      
    } else if (isNextTok(Tokens.While)) {
      scan();
      t = new WhileTree();
      
      t.addKid(rExpr());
      t.addKid(rBlock());
      
      return t;
    } else if (isNextTok(Tokens.Unless)) {        
      scan();
      t = new UnlessTree();
      
      t.addKid(rExpr());
      
      expect(Tokens.Then);
      t.addKid(rBlock());
      
      return t;
    } else if (isNextTok(Tokens.Select)) {        
      scan();
      t = new SelectTree();
      
      t.addKid(rName());
      
      t.addKid(rSelectBlock());
      return t;
    } else if (isNextTok(Tokens.Return)) {
      scan();
      t = new ReturnTree();
      
      t.addKid(rExpr());
      
      return t;
    } else if (isNextTok(Tokens.LeftBrace)) {
      return rBlock();
    }
    
    t = rName();
    t = (new AssignTree()).addKid(t);
    
    expect(Tokens.Assign);
    
    t.addKid(rExpr());
    
    return t;
  }
  
  /**
  * Select:
  * SELECT_BLOCK -> '[' SELECTOR+ ']'
  * SELECTOR -> '[' E ']' '->' BLOCK
  * @return block tree
  * @exception SyntaxError - thrown for any syntax error e.g. an expected
  *                        left brace isn't found
  */
  
  private AST rSelectBlock() throws SyntaxError {
    expect(Tokens.LeftBrace);
    AST t = new SelectBlockTree();
    
    boolean selectorFound = false;
    while (currentToken.getKind() == Tokens.LeftBracket){
      t.addKid(rSelector());
      selectorFound = true;
    } 
    
    expect(Tokens.RightBrace);
    
    if (!selectorFound) {
      throw new SyntaxError(currentToken, Tokens.Identifier);
    }
    
    return t;
  }
  
  
  private AST rSelector() throws SyntaxError {
    expect(Tokens.LeftBracket);
    AST t = new SelectorTree();
    
    t.addKid(rExpr());
    
    expect(Tokens.RightBracket);
    expect(Tokens.Arrow);
    
    t.addKid(rBlock());
    return t;
  }
  
  
  /**
  * Expressions (Lowest Priority):
  * E -> SE
  * E -> SE '==' SE
  * E -> SE '!=' SE
  * E -> SE '<' SE
  * E -> SE '<=' SE
  * E -> SE '>' SE
  * E -> SE '>=' SE
  * @return the tree corresponding to the expression
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rExpr() throws SyntaxError {
    AST t, kid = rSimpleExpr();
    
    t = getRelationTree();
    if (t == null) {
      return kid;
    }
    
    t.addKid(kid);
    t.addKid(rSimpleExpr());
    
    return t;
  }
  
  /**
  * Simple Expressions (Addition Operators/Second Lowest Priority):
  * SE -> T
  * SE -> SE '+' T
  * SE -> SE '-' T
  * SE -> SE '|' T
  *
  * This rule indicates we should pick up as many Terms as
  * possible; the Terms will be left associative
  *
  * @return the tree corresponding to the adding expression
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rSimpleExpr() throws SyntaxError {
    AST t, kid = rTerm();
    
    while ((t = getAddOperTree()) != null) {
      t.addKid(kid);
      t.addKid(rTerm());
      
      kid = t;
    }
    
    return kid;
  }
  
  /**
  * Terms (Multiplication Operators/Highest Priority):
  * T -> F
  * T -> T '*' F
  * T -> T '/' F
  * T -> T '&' F
  * T -> T '%' F
  * This rule indicates we should pick up as many Factors as
  * possible; the Factors will be left associative
  *
  * @return the tree corresponding to the multiplying expression
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rTerm() throws SyntaxError {
    AST t, kid = rFactor();
    
    while ((t = getMultOperTree()) != null) {
      t.addKid(kid);
      t.addKid(rFactor());
      
      kid = t;
    }
    
    return kid;
  }
  
  /**
  * Factors (Parenthesized expressions, identifiers, litereals, function calls):
  * F -> '(' E ')'
  * F -> NAME
  * F -> <int>
  * F -> <string>
  * F -> <hex>
  * F -> NAME '(' (E list ',')? ')'
  * 
  * @return the tree corresponding to the factor expression
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rFactor() throws SyntaxError {
    AST t;
    
    // -> (e)
    if (isNextTok(Tokens.LeftParen)) {
      scan();
      t = rExpr();
      expect(Tokens.RightParen);
      return t;
    }
    // -> <int>
    else if (isNextTok(Tokens.INTeger)) {
      t = new IntTree(currentToken);
      scan();
      return t;
    }
    // -> <string>  
    else if (isNextTok(Tokens.StringLit)){
      t = new StringTree(currentToken);
      scan();
      return t;
    }
    // -> <hex> 
    else if (isNextTok(Tokens.HexLit)){
      t = new HexTree(currentToken);
      scan();
      return t;
    }
    
    t = rName();
    // -> name (not a function call)
    if (!isNextTok(Tokens.LeftParen)) {
      return t;
    }
    
    // -> name '(' (e list ',')? ) ==> call
    scan();
    t = (new CallTree()).addKid(t);
    
    if (!isNextTok(Tokens.RightParen)) {
      do {
        t.addKid(rExpr());
        if (isNextTok(Tokens.Comma)) {
          scan();
        } else {
          break;
        }
      } while (true);
    }
    expect(Tokens.RightParen);
    
    return t;
  }
  
  /**
  * Identifier:
  * NAME -> <id>
  *
  * @return the id tree
  * @exception SyntaxError - thrown for any syntax error
  */
  public AST rName() throws SyntaxError {
    AST t;
    if (isNextTok(Tokens.Identifier)) {
      t = new IdTree(currentToken);
      scan();
      
      return t;
    } else {
      throw new SyntaxError(currentToken, Tokens.Identifier);
    }
  }
  
  // build tree with current token's relation
  private AST getRelationTree() {
    Tokens kind = currentToken.getKind();
    
    if (relationalOps.contains(kind)) {
      AST t = new RelOpTree(currentToken);
      scan();
      
      return t;
    } else {
      return null;
    }
  }
  
  private AST getAddOperTree() {
    Tokens kind = currentToken.getKind();
    
    if (addingOps.contains(kind)) {
      AST t = new AddOpTree(currentToken);
      scan();
      
      return t;
    } else {
      return null;
    }
  }
  
  private AST getMultOperTree() {
    Tokens kind = currentToken.getKind();
    
    if (multiplyingOps.contains(kind)) {
      AST t = new MultOpTree(currentToken);
      scan();
      
      return t;
    } else {
      return null;
    }
  }
  
  private boolean isNextTok(Tokens kind) {
    return currentToken != null && currentToken.getKind() == kind;
  }
  
  private void expect(Tokens kind) throws SyntaxError {
    if (isNextTok(kind)) {
      scan();
      
      return;
    }
    throw new SyntaxError(currentToken, kind);
  }
  
  private void scan() {
    currentToken = lex.nextToken();
    
    return;
  }
}