package tests.requirements.constrainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ast.AST;
import ast.BlockTree;
import ast.BoolTypeTree;
import ast.DeclTree;
import ast.IdTree;
import ast.IfTree;
import ast.IntTree;
import ast.IntTypeTree;
import ast.ProgramTree;
import ast.RelOpTree;
import constrain.Constrainer;
import constrain.ConstraintError;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestLexer;

public class IfTest {
  @BeforeEach
  public void setUp() {
    AST.NodeCount = 0;
  }

  @Test
  void testIfWithoutElseConstraint() throws Exception {
    AST tree = buildValidIf();

    Constrainer constrainer = new Constrainer(tree, new Parser(new TestLexer()));
    constrainer.execute();

    AST relOpDecoration = tree.getKid(1).getKid(2).getKid(1).getDecoration();
    AST decorationType = relOpDecoration.getKid(1);
    IdTree decorationId = (IdTree) relOpDecoration.getKid(2);

    assertEquals(BoolTypeTree.class, decorationType.getClass());
    assertEquals("<<bool>>", decorationId.getSymbol().toString());
  }

  @Test
  void testInvalidIfWithoutElse() throws Exception {
    AST tree = buildInvalidIf();

    try {
      Constrainer constrainer = new Constrainer(tree, new Parser(new TestLexer()));
      constrainer.execute();
    } catch (ConstraintError error) {
      assertTrue(error.getMessage().contains("BadConditional"));
      return;
    }

    assertTrue(false, "Failed to throw a BadConditional constraint error");
  }

  private static AST buildValidIf() {
    AST program = new ProgramTree();

    AST block = new BlockTree();
    program.addKid(block);

    AST typeTree = new IntTypeTree();
    AST declIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    AST decl = new DeclTree().addKid(typeTree).addKid(declIdentifier);
    block.addKid(decl);

    AST ifTree = new IfTree();
    AST compareIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    AST relOpTree = new RelOpTree(Helpers.getTestToken("=="));
    AST intTree = new IntTree(Helpers.getTestToken("int"));
    relOpTree.addKid(compareIdentifier).addKid(intTree);
    ifTree.addKid(relOpTree).addKid(new BlockTree());
    block.addKid(ifTree);

    return program;
  }

  private static AST buildInvalidIf() {
    AST program = new ProgramTree();

    AST block = new BlockTree();
    program.addKid(block);

    AST ifTree = new IfTree();
    AST intTree = new IntTree(Helpers.getTestToken("int"));
    ifTree.addKid(intTree).addKid(new BlockTree());
    block.addKid(ifTree);

    return program;
  }
}
