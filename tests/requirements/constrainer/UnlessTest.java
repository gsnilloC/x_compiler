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
import ast.IntTree;
import ast.IntTypeTree;
import ast.ProgramTree;
import ast.RelOpTree;
import ast.UnlessTree;
import constrain.Constrainer;
import constrain.ConstraintError;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestLexer;

public class UnlessTest {
  @BeforeEach
  public void setUp() {
    AST.NodeCount = 0;
  }

  @Test
  void testUnlessConstraint() throws Exception {
    AST tree = buildValidUnless();

    Constrainer constrainer = new Constrainer(tree, new Parser(new TestLexer()));
    constrainer.execute();

    AST relOpDecoration = tree.getKid(1).getKid(2).getKid(1).getDecoration();
    AST decorationType = relOpDecoration.getKid(1);
    IdTree decorationId = (IdTree) relOpDecoration.getKid(2);

    assertEquals(BoolTypeTree.class, decorationType.getClass());
    assertEquals("<<bool>>", decorationId.getSymbol().toString());
  }

  @Test
  void testInvalidUnless() throws Exception {
    AST tree = buildInvalidUnless();

    try {
      Constrainer constrainer = new Constrainer(tree, new Parser(new TestLexer()));
      constrainer.execute();
    } catch (ConstraintError error) {
      assertTrue(error.getMessage().contains("BadConditional"));
      return;
    }

    assertTrue(false, "Failed to throw a BadConditional constraint error");
  }

  private static AST buildValidUnless() {
    AST program = new ProgramTree();

    AST block = new BlockTree();
    program.addKid(block);

    AST typeTree = new IntTypeTree();
    AST declIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    AST decl = new DeclTree().addKid(typeTree).addKid(declIdentifier);
    block.addKid(decl);

    AST unlessTree = new UnlessTree();
    AST compareIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    AST relOpTree = new RelOpTree(Helpers.getTestToken("=="));
    AST intTree = new IntTree(Helpers.getTestToken("int"));
    relOpTree.addKid(compareIdentifier).addKid(intTree);
    unlessTree.addKid(relOpTree).addKid(new BlockTree());
    block.addKid(unlessTree);

    return program;
  }

  private static AST buildInvalidUnless() {
    AST program = new ProgramTree();

    AST block = new BlockTree();
    program.addKid(block);

    AST unlessTree = new UnlessTree();
    AST intTree = new IntTree(Helpers.getTestToken("int"));
    unlessTree.addKid(intTree).addKid(new BlockTree());
    block.addKid(unlessTree);

    return program;
  }
}
