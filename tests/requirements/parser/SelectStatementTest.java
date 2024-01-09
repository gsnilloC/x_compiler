package tests.requirements.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ast.AST;
import ast.AssignTree;
import ast.BlockTree;
import ast.IdTree;
import ast.IntTree;
import ast.ProgramTree;
import ast.SelectBlockTree;
import ast.SelectTree;
import ast.SelectorTree;
import parser.Parser;
import parser.SyntaxError;
import tests.helpers.Helpers;
import tests.helpers.TestVisitor;
import visitor.ASTVisitor;

public class SelectStatementTest {

  private static final String INVALID_SELECT_PROGRAM = String.join(
      System.lineSeparator(),
      List.of(
          "program {",
          "  select {",
          "  }",
          "}"));

  @Test
  public void testInvalidSelectStatement() throws Exception {
    final Parser parser = new Parser(
        Helpers.lexerFromPseudoProgram(INVALID_SELECT_PROGRAM));

    try {
      parser.execute();
      assertEquals(true, false, "The parser did not throw a Syntax Error for a select statement with no selectors");
    } catch (SyntaxError e) {
      assertEquals(true, true);
    }
  }

  private static final String INVALID_SELECTOR_PROGRAM = String.join(
      System.lineSeparator(),
      List.of(
          "program {",
          "  select <id> {",
          "  }",
          "}"));

  @Test
  public void testInvalidSelectorStatement() throws Exception {
    final Parser parser = new Parser(
        Helpers.lexerFromPseudoProgram(INVALID_SELECTOR_PROGRAM));

    try {
      parser.execute();
      assertEquals(true, false, "The parser did not throw a Syntax Error for a select statement with no selectors");
    } catch (SyntaxError e) {
      assertEquals(true, true);
    }
  }

  private static final String SINGLE_SELECTOR_PROGRAM = String.join(
      System.lineSeparator(),
      List.of(
          "program {",
          "  select <id> {",
          "    [ <int> ] -> { <id> = <int> }",
          "  }",
          "}"));

  @Test
  public void testValidSelectStatementWithSingleSelector() throws Exception {
    final Parser parser = new Parser(Helpers.lexerFromPseudoProgram(SINGLE_SELECTOR_PROGRAM));
    AST ast = parser.execute();

    ASTVisitor visitor = new TestVisitor(List.of(
        new ProgramTree(),
        new BlockTree(),
        new SelectTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new SelectBlockTree(),
        new SelectorTree(),
        new IntTree(Helpers.getTestToken("<int>")),
        new BlockTree(),
        new AssignTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new IntTree(Helpers.getTestToken("<int>"))));
    Object result = ast.accept(visitor);

    assertEquals(null, result, "An exception was thrown when parsing a select statement with a single selector");
  }

  public static final String MULTIPLE_SELECTOR_PROGRAM = String.join(
      System.lineSeparator(),
      List.of(
          "program {",
          "  select <id> {",
          "    [ <int> ] -> { <id> = <int> }",
          "    [ <int> ] -> { <id> = <int> }",
          "    [ <int> ] -> { <id> = <int> }",
          "  }",
          "}"));

  @Test
  public void testValidSelectStatementWithMultipleSelectors() throws Exception {
    final Parser parser = new Parser(Helpers.lexerFromPseudoProgram(MULTIPLE_SELECTOR_PROGRAM));
    AST ast = parser.execute();

    ASTVisitor visitor = new TestVisitor(List.of(
        new ProgramTree(),
        new BlockTree(),
        new SelectTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new SelectBlockTree(),
        new SelectorTree(),
        new IntTree(Helpers.getTestToken("<int>")),
        new BlockTree(),
        new AssignTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new IntTree(Helpers.getTestToken("<int>")),
        new SelectorTree(),
        new IntTree(Helpers.getTestToken("<int>")),
        new BlockTree(),
        new AssignTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new IntTree(Helpers.getTestToken("<int>")),
        new SelectorTree(),
        new IntTree(Helpers.getTestToken("<int>")),
        new BlockTree(),
        new AssignTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new IntTree(Helpers.getTestToken("<int>"))));
    Object result = ast.accept(visitor);

    assertEquals(null, result, "An exception was thrown when parsing a select statement with multiple selectors");
  }

}
