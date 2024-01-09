package tests.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Test;

import ast.AST;
import ast.AssignTree;
import ast.BlockTree;
import ast.DeclTree;
import ast.FormalsTree;
import ast.FunctionDeclTree;
import ast.IdTree;
import ast.IntTree;
import ast.IntTypeTree;
import ast.ProgramTree;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestVisitor;
import visitor.ASTVisitor;

public class DeclarationTest {

  private static final String VALID_DECLARATION = String.join(
      System.lineSeparator(),
      List.of(
          "program { int <id> }"));

  @Test
  public void testVariableDeclaration() throws Exception {
    final Parser parser = new Parser(Helpers.lexerFromPseudoProgram(VALID_DECLARATION));

    AST ast = parser.execute();
    ASTVisitor visitor = new TestVisitor(List.of(
        new ProgramTree(),
        new BlockTree(),
        new DeclTree(),
        new IntTypeTree(),
        new IdTree(Helpers.getTestToken("<id>"))));

    Object result = ast.accept(visitor);

    assertEquals(null, result);
  }

  private static final String VALID_FUNCTION_DECLARATION = String.join(
      System.lineSeparator(),
      List.of(
          "program {",
          "  int <id> ( int <id> , int <id> ) {",
          "    int <id>",
          "    <id> = <int>",
          "  }",
          "}"));

  @Test
  public void testFunctionDeclaration() throws Exception {
    final Parser parser = new Parser(Helpers.lexerFromPseudoProgram(VALID_FUNCTION_DECLARATION));

    AST ast = parser.execute();
    ASTVisitor visitor = new TestVisitor(List.of(
        new ProgramTree(),
        new BlockTree(),
        new FunctionDeclTree(),
        new IntTypeTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new FormalsTree(),
        new DeclTree(),
        new IntTypeTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new DeclTree(),
        new IntTypeTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new BlockTree(),
        new DeclTree(),
        new IntTypeTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new AssignTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new IntTree(Helpers.getTestToken("<int>"))));

    Object result = ast.accept(visitor);

    assertEquals(null, result);
  }
}
