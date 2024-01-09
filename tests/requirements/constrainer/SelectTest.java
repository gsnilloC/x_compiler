package tests.requirements.constrainer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ast.AST;
import ast.BlockTree;
import ast.DeclTree;
import ast.HexTree;
import ast.IdTree;
import ast.IntTree;
import ast.IntTypeTree;
import ast.ProgramTree;
import ast.SelectBlockTree;
import ast.SelectTree;
import ast.SelectorTree;
import constrain.Constrainer;
import constrain.ConstraintError;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestLexer;

public class SelectTest {
  @BeforeEach
  public void setUp() {
    AST.NodeCount = 0;
  }

  @ParameterizedTest
  @MethodSource("provideValidSelects")
  public void testValidSelect(AST tree) throws Exception {
    Constrainer constrainer = new Constrainer(tree, new Parser(new TestLexer()));
    constrainer.execute();

    // Identifier should be decorated with declaration
    AST declaration = tree.getKid(1).getKid(1);
    AST intrinsicType = declaration.getKid(2).getDecoration();

    assertNotNull(intrinsicType);
    // Make sure its the correct type
    assertEquals(IntTypeTree.class, intrinsicType.getKid(1).getClass());
    assertEquals(IdTree.class, intrinsicType.getKid(2).getClass());
    assertEquals(((IdTree) intrinsicType.getKid(2)).getSymbol().toString(), "<<int>>");

    AST selectIdentifier = tree.getKid(1).getKid(2).getKid(1);

    assertEquals(declaration, selectIdentifier.getDecoration(), "Select's identifier is incorrectly decorated");

    // Selector expressions should be decorated with intrinsic type tree
    for (Iterator<AST> iterator = tree.getKid(1).getKid(2).getKid(2).getKids().iterator(); iterator.hasNext();) {
      assertEquals(intrinsicType, iterator.next().getKid(1).getDecoration());
    }
  }

  @Test
  public void testInvalidSelect() throws Exception {
    AST tree = invalidSelect();

    try {
      Constrainer constrainer = new Constrainer(tree, new Parser(new TestLexer()));
      constrainer.execute();
    } catch (ConstraintError error) {
      assertTrue(error.getMessage().contains("SelectorTypeMismatch"));
      return;
    }

    assertEquals(true, false, "Failed to throw a SelectorTypeMismatch constraint error");
  }

  private static Stream<Arguments> provideValidSelects() {
    return Stream.of(Arguments.of(createValidSingleSelect()), Arguments.of(createValidMultiSelect()));
  }

  private static AST createValidSingleSelect() {
    AST program = new ProgramTree();

    AST block = new BlockTree();
    program.addKid(block);

    AST intType = new IntTypeTree();
    AST declIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    AST decl = new DeclTree().addKid(intType).addKid(declIdentifier);
    block.addKid(decl);

    AST select = new SelectTree();
    block.addKid(select);

    AST selectIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    select.addKid(selectIdentifier);

    AST selectBlock = new SelectBlockTree();
    select.addKid(selectBlock);

    AST selector = new SelectorTree();
    selectBlock.addKid(selector);

    AST intTree = new IntTree(Helpers.getTestToken("int"));
    AST selectorBlock = new BlockTree();
    selector.addKid(intTree).addKid(selectorBlock);

    return program;
  }

  private static AST createValidMultiSelect() {
    AST program = new ProgramTree();

    AST block = new BlockTree();
    program.addKid(block);

    AST intType = new IntTypeTree();
    AST declIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    AST decl = new DeclTree().addKid(intType).addKid(declIdentifier);
    block.addKid(decl);

    AST select = new SelectTree();
    block.addKid(select);

    AST selectIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    select.addKid(selectIdentifier);

    AST selectBlock = new SelectBlockTree();
    select.addKid(selectBlock);

    AST selector = new SelectorTree();
    selectBlock.addKid(selector);

    AST intTree = new IntTree(Helpers.getTestToken("int"));
    AST selectorBlock = new BlockTree();
    selector.addKid(intTree).addKid(selectorBlock);

    AST secondSelector = new SelectorTree();
    selectBlock.addKid(secondSelector);

    AST secondIntTree = new IntTree(Helpers.getTestToken("int"));
    AST secondSelectorBlock = new BlockTree();
    secondSelector.addKid(secondIntTree).addKid(secondSelectorBlock);

    AST thirdSelector = new SelectorTree();
    selectBlock.addKid(thirdSelector);

    AST thirdIntTree = new IntTree(Helpers.getTestToken("int"));
    AST thirdSelectorBlock = new BlockTree();
    thirdSelector.addKid(thirdIntTree).addKid(thirdSelectorBlock);

    return program;
  }

  private static AST invalidSelect() {
    AST program = new ProgramTree();

    AST block = new BlockTree();
    program.addKid(block);

    AST intType = new IntTypeTree();
    AST declIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    AST decl = new DeclTree().addKid(intType).addKid(declIdentifier);
    block.addKid(decl);

    AST select = new SelectTree();
    block.addKid(select);

    AST selectIdentifier = new IdTree(Helpers.getTestToken("<id>"));
    select.addKid(selectIdentifier);

    AST selectBlock = new SelectBlockTree();
    select.addKid(selectBlock);

    AST selector = new SelectorTree();
    selectBlock.addKid(selector);

    AST hexTree = new HexTree(Helpers.getTestToken("hex"));
    AST selectorBlock = new BlockTree();
    selector.addKid(hexTree).addKid(selectorBlock);

    return program;
  }

}
