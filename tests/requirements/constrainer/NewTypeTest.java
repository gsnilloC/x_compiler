package tests.requirements.constrainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ast.AST;
import ast.AssignTree;
import ast.BlockTree;
import ast.DeclTree;
import ast.HexTree;
import ast.HexTypeTree;
import ast.IdTree;
import ast.ProgramTree;
import ast.StringTree;
import ast.StringTypeTree;
import constrain.Constrainer;
import lexer.Token;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestLexer;

public class NewTypeTest {
  @BeforeEach
  public void setUp() {
    AST.NodeCount = 0;
  }

  @ParameterizedTest
  @MethodSource("provideTestTrees")
  void testNewTypes(AST tree, Class<?> expectedIntrinsicType, String expectedIntrinsicDummyVariable) throws Exception {
    final Constrainer constrainer = new Constrainer(tree, new Parser(new TestLexer()));
    constrainer.execute();

    AST declarationNode = tree.getKid(1).getKid(1);

    // Test that the identifier declaration is decorated with its intrinsic type
    // tree,
    // and that the dummy variable naming convention is followed
    AST declaredIdentifierDecoration = declarationNode.getKid(2).getDecoration();
    assertEquals(expectedIntrinsicType, declaredIdentifierDecoration.getKid(1).getClass());
    assertEquals(expectedIntrinsicDummyVariable,
        ((IdTree) declaredIdentifierDecoration.getKid(2)).getSymbol().toString());

    // Test that the identifier, when used in a statement, is decorated with its
    // declaration
    AST statementIdentifierDecoration = tree.getKid(1).getKid(2).getKid(1).getDecoration();
    assertEquals(statementIdentifierDecoration, declarationNode);
  }

  private static Stream<Arguments> provideTestTrees() throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
    return Stream.of(
        Arguments.of(createTestTree(HexTypeTree.class, HexTree.class, "<hex>"), HexTypeTree.class, "<<hex>>"),
        Arguments.of(createTestTree(StringTypeTree.class, StringTree.class, "<string>"), StringTypeTree.class,
            "<<string>>")

    );
  }

  private static AST createTestTree(Class<?> type, Class<?> literal, String lit)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      SecurityException, NoSuchMethodException {
    AST program = new ProgramTree();

    AST block = new BlockTree();
    program.addKid(block);

    AST typeTree = (AST) type.getConstructors()[0].newInstance();
    AST declIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    AST decl = new DeclTree().addKid(typeTree).addKid(declIdentifier);
    block.addKid(decl);

    AST assignmentIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    AST assignment = new AssignTree();
    AST litTree = (AST) literal.getConstructor(Token.class).newInstance(Helpers.getTestToken(lit));
    assignment.addKid(assignmentIdentifier).addKid(litTree);
    block.addKid(assignment);

    return program;

  }
}
