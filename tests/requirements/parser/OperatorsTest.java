package tests.requirements.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ast.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lexer.ILexer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestVisitor;
import visitor.*;

public class OperatorsTest {

  @ParameterizedTest
  @MethodSource("provideTokenPrograms")
  void testOperators(ILexer lexer, List<AST> expectedAst)
      throws Exception {
    Parser parser = new Parser(lexer);
    AST ast = parser.execute();

    // Helpful for debugging (please remember to comment before submission!):
    // PrintVisitor printer = new PrintVisitor();
    // ast.accept(printer);

    ASTVisitor visitor = new TestVisitor(expectedAst);
    Object result = ast.accept(visitor);

    assertEquals(null, result);
  }

  private static Stream<Arguments> provideTokenPrograms()
      throws Exception {
    return Stream.of(
        Arguments.of(lexerForRelop("=="), expectedAstForRelop("==")),
        Arguments.of(lexerForRelop("!="), expectedAstForRelop("!=")),
        Arguments.of(lexerForRelop("<"), expectedAstForRelop("<")),
        Arguments.of(lexerForRelop("<="), expectedAstForRelop("<=")),
        Arguments.of(lexerForRelop(">"), expectedAstForRelop(">")),
        Arguments.of(lexerForRelop(">="), expectedAstForRelop(">=")),
        Arguments.of(lexerForMultOp("%"), expectedAstForMultOp("%")));
  }

  private static ILexer lexerForMultOp(String op) throws Exception {
    return Helpers.lexerFromPseudoProgram(
        String.format(
            String.join(
                System.lineSeparator(),
                "program { int <id>",
                "<id> = <int> %s <int>",
                "}"),
            op));
  }

  private static List<AST> expectedAstForMultOp(String op) {
    return Arrays.asList(
        new ProgramTree(),
        new BlockTree(),
        new DeclTree(),
        Helpers.getTestAst("int"),
        new IdTree(Helpers.getTestToken("<id>")),

        new AssignTree(),
        new IdTree(Helpers.getTestToken("<id>")),
        new MultOpTree(Helpers.getTestToken(op)),
        new IntTree(Helpers.getTestToken("<int>")),
        new IntTree(Helpers.getTestToken("<int>")));
  }

  private static ILexer lexerForRelop(String relop) throws Exception {
    return Helpers.lexerFromPseudoProgram(
        String.format(
            String.join(
                System.lineSeparator(),
                "program {",
                "return <int> %s <int>",
                "}"),
            relop));
  }

  private static List<AST> expectedAstForRelop(String relop) {
    return Arrays.asList(
        new ProgramTree(),
        new BlockTree(),
        new ReturnTree(),
        new RelOpTree(Helpers.getTestToken(relop)),
        new IntTree(Helpers.getTestToken("<int>")),
        new IntTree(Helpers.getTestToken("<int>")));
  }
}
