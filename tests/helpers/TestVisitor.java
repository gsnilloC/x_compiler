package tests.helpers;

import ast.*;
import java.util.List;
import visitor.ASTVisitor;

public class TestVisitor extends ASTVisitor {

  private List<AST> expected;
  private int index;

  public TestVisitor(final List<AST> expected) {
    this.expected = expected;
    this.index = 0;
  }

  private Object test(AST t) {
    try {
      expected.get(index).getClass().cast(t);

      index++;
      return testKids(t);
    } catch (ClassCastException exception) {
      return String.format(
          "Expected [%s] but got [%s]",
          expected.get(index).getClass().getSimpleName(),
          t.getClass().getSimpleName());
    }
  }

  private Object test(AST t, String expectedSymbol, String actualSymbol) {
    try {
      expected.get(index).getClass().cast(t);

      if (!expectedSymbol.equals(actualSymbol)) {
        throw new Exception(
            String.format(
                "Expected [%s] but got [%s]",
                expectedSymbol,
                actualSymbol));
      }

      index++;
      return testKids(t);
    } catch (ClassCastException exception) {
      return String.format(
          "Expected [%s] but got [%s]",
          expected.get(index).getClass().getSimpleName(),
          t.getClass().getSimpleName());
    } catch (Exception exception) {
      return exception.getMessage();
    }
  }

  private Object testKids(AST t) {
    for (AST kid : t.getKids()) {
      Object result = kid.accept(this);

      if (result != null) {
        return result;
      }
    }

    return null;
  }

  @Override
  public Object visitProgramTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitBlockTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitFunctionDeclTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitCallTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitDeclTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitIntTypeTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitBoolTypeTree(AST t) {
    return test(t);
  }

  public Object visitBoolTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitFormalsTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitActualArgsTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitIfTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitWhileTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitReturnTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitAssignTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitIntTree(AST t) {
    String actualSymbol = ((IntTree) t).getSymbol().toString();
    String expectedSymbol = ((IntTree) expected.get(index)).getSymbol().toString();

    return test(t, expectedSymbol, actualSymbol);
  }

  @Override
  public Object visitIdTree(AST t) {
    String actualSymbol = ((IdTree) t).getSymbol().toString();
    String expectedSymbol = ((IdTree) expected.get(index)).getSymbol().toString();

    return test(t, expectedSymbol, actualSymbol);
  }

  @Override
  public Object visitRelOpTree(AST t) {
    String actualSymbol = ((RelOpTree) t).getSymbol().toString();
    String expectedSymbol = ((RelOpTree) expected.get(index)).getSymbol().toString();

    return test(t, expectedSymbol, actualSymbol);
  }

  @Override
  public Object visitAddOpTree(AST t) {
    String actualSymbol = ((AddOpTree) t).getSymbol().toString();
    String expectedSymbol = ((AddOpTree) expected.get(index)).getSymbol().toString();

    return test(t, expectedSymbol, actualSymbol);
  }

  @Override
  public Object visitMultOpTree(AST t) {
    String actualSymbol = ((MultOpTree) t).getSymbol().toString();
    String expectedSymbol = ((MultOpTree) expected.get(index)).getSymbol().toString();

    return test(t, expectedSymbol, actualSymbol);
  }

  @Override
  public Object visitHexTypeTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitStringTypeTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitHexTree(AST t) {
    String actualSymbol = ((HexTree) t).getSymbol().toString();
    String expectedSymbol = ((HexTree) expected.get(index)).getSymbol().toString();

    return test(t, expectedSymbol, actualSymbol);
  }

  @Override
  public Object visitStringTree(AST t) {
    String actualSymbol = ((StringTree) t).getSymbol().toString();
    String expectedSymbol = ((StringTree) expected.get(index)).getSymbol().toString();

    // Just in case people left quotes on
    return test(t, expectedSymbol, actualSymbol);
  }

  @Override
  public Object visitUnlessTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitSelectTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitSelectBlockTree(AST t) {
    return test(t);
  }

  @Override
  public Object visitSelectorTree(AST t) {
    return test(t);
  }
}
