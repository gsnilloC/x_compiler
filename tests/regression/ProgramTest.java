package tests.regression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Test;

import ast.AST;
import ast.BlockTree;
import ast.ProgramTree;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestVisitor;
import visitor.ASTVisitor;

public class ProgramTest {
  @Test
  public void testProgram() throws Exception {
    final Parser parser = new Parser(Helpers.lexerFromPseudoProgram("program { }"));

    AST ast = parser.execute();
    ASTVisitor visitor = new TestVisitor(List.of(new ProgramTree(), new BlockTree()));

    Object result = ast.accept(visitor);

    assertEquals(null, result);
  }
}
