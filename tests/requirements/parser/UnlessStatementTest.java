package tests.requirements.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ast.*;
import tests.helpers.Helpers;
import tests.helpers.TestVisitor;
import visitor.ASTVisitor;
import lexer.ILexer;
import parser.Parser;

public class UnlessStatementTest {

  private static final String TEST_CONTENT = String.join(
      System.lineSeparator(),
      List.of(
          "program { int <id>",
          "  unless ( <int> == <int> ) then {",
          "    <id> = <int>",
          "  }",
          "}"));

  @Test
  void testUnlessStatement() throws Exception {
    ILexer lexer = Helpers.lexerFromPseudoProgram(TEST_CONTENT);
    final Parser parser = new Parser(lexer);
    AST ast = parser.execute();

    ASTVisitor visitor = new TestVisitor(List.of(
        new ProgramTree(),
        new BlockTree(),
        new DeclTree(),
        new IntTypeTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new UnlessTree(),
        new RelOpTree(Helpers.getTestToken("==")),
        new IntTree(Helpers.getTestToken("<int>")),
        new IntTree(Helpers.getTestToken("<int>")),
        new BlockTree(),
        new AssignTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new IntTree(Helpers.getTestToken("<int>"))));
    Object result = ast.accept(visitor);

    assertEquals(null, result);
  }
}
