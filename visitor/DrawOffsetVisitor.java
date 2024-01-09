package visitor;

import ast.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
*
* @author Lowell Milliken
*  updated by Ryan Shu
*/
public class DrawOffsetVisitor extends ASTVisitor {
  
  private int nodew = 85;
  private int nodeh = 40;
  private int vertSep = 50;
  private int horizSep = 0;
  
  private int width;
  private int height;
  private int [] nCount;
  
  private int [] progress;
  private int depth = 0;
  private BufferedImage bimg;
  private Graphics2D g2;
  private HashMap<AST, Integer> intOffsets;
  
  public DrawOffsetVisitor(int [] nCount, HashMap<AST, Integer> intOffsets, int maxOffset) {
    this.nCount = nCount;
    this.intOffsets = intOffsets;
    progress = new int[ nCount.length ];
    
    
    height = nCount.length * ( nodeh + vertSep);
    width = (maxOffset) * ( nodew + horizSep);
    
    
    g2 = createGraphics2D( width, height );
    
  }
  
  public void draw( String s, AST t ) {
    int hstep = nodew  + horizSep;
    int vstep = Math.max((int) ((double) (height - nodeh) / (double) (nCount.length - 1)), 50);
    
    int y = depth * vstep;
    int x = intOffsets.get(t)* hstep;
    
    g2.setColor( Color.black );
    g2.drawOval( x, y, nodew, nodeh );
    g2.setColor(Color.pink);
    g2.fillOval(x, y, nodew, nodeh);
    g2.setColor( Color.BLACK );
    g2.drawString(s, x + nodew/2 - g2.getFontMetrics().stringWidth(s)/2, y + nodeh/2 + g2.getFontMetrics().getAscent()/2 );
    
    int startx = x + nodew / 2;
    int starty = y + nodeh;
    int endx;
    int endy;
    g2.setColor( Color.black );
    
    for( int i = 0; i < t.kidCount(); i++ ) {
      endx = (intOffsets.get( t.getKid( i+1 )) * hstep) + nodew / 2;
      endy = ( depth + 1 ) * vstep;
      g2.drawLine( startx, starty, endx, endy );
    }
    
    progress[ depth ]++;
    depth++;
    visitKids( t );
    depth--;

  }
  
  private Graphics2D createGraphics2D( int w, int h ) {
    Graphics2D g2;
    
    if( bimg == null || bimg.getWidth() != w || bimg.getHeight() != h ) {
      bimg = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB );
    }
    
    g2 = bimg.createGraphics();
    g2.setBackground( Color.WHITE );
    g2.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.clearRect( 0, 0, w, h );
    return g2;
  }
  
  public BufferedImage getImage() {
    return bimg;
  }
  
  @Override
  public Object visitProgramTree(AST t) {
    draw("Program", t);
    return null;
  }
  
  @Override
  public Object visitBlockTree(AST t) {
    draw("Block", t);
    return null;
  }
  
  @Override
  public Object visitFunctionDeclTree(AST t) {
    draw("FunctionDecl", t);
    return null;
  }
  
  @Override
  public Object visitCallTree(AST t) {
    draw("Call", t);
    return null;
  }
  
  @Override
  public Object visitDeclTree(AST t) {
    draw("Decl", t);
    return null;
  }
  
  @Override
  public Object visitIntTypeTree(AST t) {
    draw("IntType", t);
    return null;
  }
  
  @Override
  public Object visitBoolTypeTree(AST t) {
    draw("BoolType", t);
    return null;
  }
  
  @Override
  public Object visitFormalsTree(AST t) {
    draw("Formals", t);
    return null;
  }
  
  @Override
  public Object visitActualArgsTree(AST t) {
    draw("ActualArgs", t);
    return null;
  }
  
  @Override
  public Object visitIfTree(AST t) {
    draw("If", t);
    return null;
  }
  
  @Override
  public Object visitWhileTree(AST t) {
    draw("While", t);
    return null;
  }
  
  @Override
  public Object visitReturnTree(AST t) {
    draw("Return", t);
    return null;
  }
  
  @Override
  public Object visitAssignTree(AST t) {
    draw("Assign", t);
    return null;
  }
  
  @Override
  public Object visitIntTree(AST t) {
    draw("Int: " + ((IntTree) t).getSymbol().toString(), t);
    return null;
  }
  
  @Override
  public Object visitIdTree(AST t) {
    draw("Id: " + ((IdTree) t).getSymbol().toString(), t);
    return null;
  }
  
  @Override
  public Object visitRelOpTree(AST t) {
    draw("RelOp: " + ((RelOpTree) t).getSymbol().toString(), t);
    return null;
  }
  
  @Override
  public Object visitAddOpTree(AST t) {
    draw("AddOp: " + ((AddOpTree) t).getSymbol().toString(), t);
    return null;
  }
  
  @Override
  public Object visitMultOpTree(AST t) {
    draw("MultOp: " + ((MultOpTree) t).getSymbol().toString(), t);
    return null;
  }
  
  @Override
  public Object visitStringTypeTree(AST t) {
    draw("StringType", t);
    return null;
  }
  
  @Override
  public Object visitStringTree(AST t) {
    draw("String: " + ((StringTree) t).getSymbol().toString(), t);
    return null;
  }
  
  @Override
  public Object visitHexTypeTree(AST t) {
    draw("HexType", t);
    return null;
  }
  
  @Override
  public Object visitHexTree(AST t) {
    draw("Hex: " + ((HexTree) t).getSymbol().toString(), t);
    return null;
  }
  
  @Override
  public Object visitUnlessTree(AST t) {
    draw("Unless", t);
    return null;
  }
  
  @Override
  public Object visitSelectTree(AST t) {
    draw("Select", t);
    return null;
  }
  
  @Override
  public Object visitSelectBlockTree(AST t) {
    draw("SelectBlock", t);
    return null;
  }
  
  @Override
  public Object visitSelectorTree(AST t) {
    draw("Selector", t);
    return null;
  }
}