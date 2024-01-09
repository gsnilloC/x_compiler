package tests.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ast.AST;
import ast.AssignTree;
import ast.BlockTree;
import ast.DeclTree;
import ast.IdTree;
import ast.IfTree;
import ast.IntTree;
import ast.IntTypeTree;
import ast.ProgramTree;
import ast.RelOpTree;
import lexer.ILexer;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestVisitor;
import visitor.ASTVisitor;

public class IfElseStatementTest {

  @ParameterizedTest
  @MethodSource("provideIfStatements")
  void testIfStatement(ILexer lexer, List<AST> expectedAst) throws Exception {
    final Parser parser = new Parser(lexer);
    AST ast = parser.execute();

    // Helpful for debugging (please remember to comment before submission!):
    // PrintVisitor printer = new PrintVisitor();
    // ast.accept(printer);

    ASTVisitor visitor = new TestVisitor(expectedAst);
    Object result = ast.accept(visitor);

    assertEquals(null, result);
  }

  private static Stream<Arguments> provideIfStatements() throws Exception {
    return Stream.of(
        Arguments.of(
            Helpers.lexerFromPseudoProgram(IF_ELSE_TEST_PROGRAM),
            IF_ELSE_TEST_AST));
  }

  private static final String IF_ELSE_TEST_PROGRAM = String.join(
      System.lineSeparator(),
      List.of(
          "program { int <id>",
          "    if ( <int> == <int> ) then {",
          "        <id> = <int>",
          "    } else {",
          "        <id> = <int>",
          "    }",
          "}"));

  private static final List<AST> IF_ELSE_TEST_AST = List.of(
      new ProgramTree(),
      new BlockTree(),
      new DeclTree(),
      new IntTypeTree(),
      new IdTree(Helpers.getTestToken("<id>")),
      new IfTree(),
      new RelOpTree(Helpers.getTestToken("==")),
      new IntTree(Helpers.getTestToken("<int>")),
      new IntTree(Helpers.getTestToken("<int>")),
      new BlockTree(),
      new AssignTree(),
      new IdTree(Helpers.getTestToken("<id>")),
      new IntTree(Helpers.getTestToken("<int>")),
      new BlockTree(),
      new AssignTree(),
      new IdTree(Helpers.getTestToken("<id>")),
      new IntTree(Helpers.getTestToken("<int>")));
}
