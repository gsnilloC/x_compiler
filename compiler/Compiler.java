package compiler;

import ast.*;
import constrain.Constrainer;
import parser.Parser;
import visitor.*;

/**
 * The Compiler class contains the main program for compiling
 * a source program to bytecodes
 */
public class Compiler {

  String sourceFile;

  public Compiler(String sourceFile) {
    this.sourceFile = sourceFile;
  }

  public void compileProgram() {
    try {
      Parser parser = new Parser(sourceFile);
      AST t = parser.execute();

      System.out.println("---------------AST-------------");
      PrintVisitor pv = new PrintVisitor();
      t.accept(pv);
      Constrainer con = new Constrainer(t, parser);
      con.execute();
      System.out.println("---------------DECORATED AST-------------");
      t.accept(pv);
      System.out.println("---------------INT/BOOL/HEX/STRING TREES-------------");
      Constrainer.intTree.accept(pv);
      Constrainer.boolTree.accept(pv);
      // Constrainer.hexTree.accept(pv);
      // Constrainer.stringTree.accept(pv);
    } catch (Exception e) {
      System.out.println("********exception*******" + e.toString());
      e.printStackTrace();
    }
  }

  public static void main(String args[]) {
    if (args.length == 0) {
      System.out.println(
          "***Incorrect usage, try: java compiler.Compiler <file> [-image]");
      System.exit(1);
    }
    Compiler compiler = new Compiler(args[0]);
    compiler.compileProgram();
  }
}
