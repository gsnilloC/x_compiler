package tests.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Test;

import ast.AST;
import ast.BlockTree;
import ast.FormalsTree;
import ast.FunctionDeclTree;
import ast.IdTree;
import ast.IntTree;
import ast.IntTypeTree;
import ast.ProgramTree;
import ast.ReturnTree;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestVisitor;
import visitor.ASTVisitor;

public class ReturnStatementTest {

  private static final String returnProgram = String.join(
      System.lineSeparator(),
      List.of(
          "program {",
          "  int <id> ( ) {",
          "    return <int>",
          "  }",
          "}"));

  private static final List<AST> expectedAst = List.of(
      new ProgramTree(),
      new BlockTree(),
      new FunctionDeclTree(),
      new IntTypeTree(),
      new IdTree(Helpers.getTestToken("<id>")),
      new FormalsTree(),
      new BlockTree(),
      new ReturnTree(),
      new IntTree(Helpers.getTestToken("<int>")));

  @Test
  public void returnStatementTest() throws Exception {
    final Parser parser = new Parser(Helpers.lexerFromPseudoProgram(returnProgram));

    AST ast = parser.execute();

    ASTVisitor visitor = new TestVisitor(expectedAst);
    Object result = ast.accept(visitor);

    assertEquals(null, result);
  }
}
