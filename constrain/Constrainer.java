package constrain;

import lexer.*;
import parser.Parser;
import visitor.*;
import ast.*;
import java.util.*;

/**
 * Constrainer object will visit the AST, gather/check variable
 * type information and decorate uses of variables with their
 * declarations; the decorations will be used by the code generator
 * to provide access to the frame offset of the variable for generating
 * load/store bytecodes; <br>
 * Note that when constraining expression trees we return the type tree
 * corresponding to the result type of the expression; e.g.
 * the result of constraining the tree for 1+2*3 will be the int type
 * tree
 */
public class Constrainer extends ASTVisitor {
    public enum ConstrainerErrors {
        BadAssignmentType, CallingNonFunction, ActualFormalTypeMismatch, NumberActualsFormalsDiffer, TypeMismatchInExpr,
        BooleanExprExpected, BadConditional, ReturnNotInFunction, BadReturnExpr, SelectorTypeMismatch,
    }

    private AST t; // the AST to constrain
    private Table symtab = new Table();
    private Parser parser; // parser used with this constrainer

    /**
     * The following comment refers to the functions stack
     * declared below the comment.
     * 
     * Whenever we start constraining a function declaration
     * we push the function decl tree which indicates we're
     * in a function (to ensure that we don't attempt to return
     * from the main program - return's are only allowed from
     * within functions); it also gives us access to the return
     * type to ensure the type of the expr that is returned is
     * the same as the type declared in the function header
     */
    private Stack<AST> functions = new Stack<AST>();

    /**
     * readTree, writeTree, intTree, boolTree,falseTree, trueTree
     * are AST's that will be constructed (intrinsic trees) for
     * every program. They are constructed in the same fashion as
     * source program trees to ensure consisten processing of
     * functions, etc.
     */
    public static AST readTree, writeTree, intTree, boolTree, idTree,
            falseTree, trueTree, readId, writeId, stringTree, hexTree;

    public Constrainer(AST t, Parser parser) {
        this.t = t;
        this.parser = parser;
    }

    public void execute() {
        symtab.beginScope();
        t.accept(this);
    }

    /**
     * t is an IdTree; retrieve the pointer to its declaration
     */
    private AST lookup(AST t) {
        return (AST) (symtab.get(((IdTree) t).getSymbol()));
    }

    /**
     * Decorate the IdTree with the given decoration - its decl tree
     */
    private void enter(AST t, AST decoration) {
        symtab.put(((IdTree) t).getSymbol(), decoration);
    }

    /**
     * get the type of the current type tree
     * 
     * @param t is the type tree
     * @return the intrinsic tree corresponding to the type of t
     */
    private AST getType(AST t) {
        if (t.getClass() == IntTypeTree.class) {
            return intTree;
        } else if (t.getClass() == StringTypeTree.class) {
            return stringTree;
        } else if (t.getClass() == HexTypeTree.class) {
            return hexTree;
        } else {
            return boolTree;
        }
    }

    public void decorate(AST t, AST decoration) {
        t.setDecoration(decoration);
    }

    /**
     * @return the decoration of the tree
     */
    public AST decoration(AST t) {
        return t.getDecoration();
    }

    /**
     * build the intrinsic trees; constrain them in the same fashion
     * as any other AST
     */
    private void buildIntrinsicTrees() {
        ILexer lex = parser.getLex();

        trueTree = new IdTree(lex.anonymousIdentifierToken("true"));
        falseTree = new IdTree(lex.anonymousIdentifierToken("false"));

        readId = new IdTree(lex.anonymousIdentifierToken("read"));
        writeId = new IdTree(lex.anonymousIdentifierToken("write"));

        boolTree = (new DeclTree()).addKid(new BoolTypeTree())
                .addKid(new IdTree(lex.anonymousIdentifierToken("<<bool>>")));
        decorate(boolTree.getKid(2), boolTree);

        intTree = (new DeclTree()).addKid(new IntTypeTree())
                .addKid(new IdTree(lex.anonymousIdentifierToken("<<int>>")));
        decorate(intTree.getKid(2), intTree);

        stringTree = (new DeclTree()).addKid(new StringTypeTree())
                .addKid(new IdTree(lex.anonymousIdentifierToken("<<string>>")));
        decorate(stringTree.getKid(2), stringTree);

        hexTree = (new DeclTree()).addKid(new HexTypeTree())
                .addKid(new IdTree(lex.anonymousIdentifierToken("<<hex>>")));
        decorate(hexTree.getKid(2), hexTree);

        // read tree takes no params and returns an int
        readTree = (new FunctionDeclTree()).addKid(new IntTypeTree()).addKid(readId).addKid(new FormalsTree())
                .addKid(new BlockTree());
        readTree.accept(this);

        // write tree takes one int param and returns that value
        writeTree = (new FunctionDeclTree()).addKid(new IntTypeTree()).addKid(writeId);
        AST decl = (new DeclTree()).addKid(new IntTypeTree())
                .addKid(new IdTree(lex.anonymousIdentifierToken("dummyFormal")));
        AST formals = (new FormalsTree()).addKid(decl);
        writeTree.addKid(formals).addKid(new BlockTree());
        writeTree.accept(this);

    }

    /**
     * Constrain the program tree - visit its kid
     */
    @Override
    public Object visitProgramTree(AST t) {
        buildIntrinsicTrees();
        this.t = t;
        t.getKid(1).accept(this);
        return null;
    }

    /**
     * Constrain the Block tree:<br>
     * <ol>
     * <li>open a new scope,
     * <li>constrain the kids in this new scope,
     * <li>close the
     * scope removing any local declarations from this scope
     * </ol>
     */

    @Override
    public Object visitBlockTree(AST t) {
        symtab.beginScope();
        visitKids(t);
        symtab.endScope();

        return null;
    }

    @Override
    public Object visitSelectTree(AST t) {
        AST nameType = (AST) t.getKid(1).accept(this);

        AST selectBlock = t.getKid(2);
        AST firstSelectorType = (AST) selectBlock.getKid(1).accept(this);
        selectBlock.accept(this);

        if (!firstSelectorType.equals(nameType)) {
            constraintError(ConstrainerErrors.SelectorTypeMismatch);
        }

        decorate(t, nameType);
        return null;
    }

    @Override
    public Object visitSelectBlockTree(AST t) {
        AST firstSelectorType = (AST) t.getKid(1).accept(this);

        for (int i = 2; i < t.kidCount(); i++) {
            AST selectorExpressionType = (AST) t.getKid(i).accept(this);

            if (!selectorExpressionType.equals(firstSelectorType)) {
                constraintError(ConstrainerErrors.SelectorTypeMismatch);
                break;
            }
        }
        return firstSelectorType;
    }

    @Override
    public Object visitSelectorTree(AST t) {
        AST expressionType = (AST) t.getKid(1).accept(this);

        t.getKid(2).accept(this);

        decorate(t, expressionType);
        return expressionType;
    }

    /**
     * Constrain the FunctionDeclTree:
     * <ol>
     * <li>Enter the function name in the current scope,
     * <li>enter the formals
     * in the function scope and
     * <li>constrain the body of the function
     * </ol>
     */
    @Override
    public Object visitFunctionDeclTree(AST t) {
        AST fname = t.getKid(2), returnType = t.getKid(1), formalsTree = t.getKid(3), bodyTree = t.getKid(4);

        functions.push(t);
        // enter function name in CURRENT scope
        enter(fname, t);

        decorate(returnType, getType(returnType));
        // new scope for formals and body
        symtab.beginScope();
        // all formal names go in new scope
        visitKids(formalsTree);

        bodyTree.accept(this);
        symtab.endScope();
        functions.pop();

        return null;
    }

    /**
     * Constrain the Call tree:<br>
     * check that the number and types of the actuals match the
     * number and type of the formals
     */
    @Override
    public Object visitCallTree(AST t) {
        AST fct, fname = t.getKid(1), fctType;
        visitKids(t);

        fct = lookup(fname);
        if (fct.getClass() != FunctionDeclTree.class) {
            constraintError(ConstrainerErrors.CallingNonFunction);
        }
        fctType = decoration(fct.getKid(1));

        decorate(t, fctType);
        decorate(t.getKid(1), fct);

        // now check that the number/types of actuals match the
        // number/types of formals
        checkArgDecls(t, fct);
        return fctType;
    }

    private void checkArgDecls(AST caller, AST fct) {
        // check number and types of args/formals match
        AST formals = fct.getKid(3);
        Iterator<AST> actualKids = caller.getKids().iterator(), formalKids = formals.getKids().iterator();

        // skip past fct name
        actualKids.next();
        for (; actualKids.hasNext();) {
            try {
                AST actualDecl = decoration(actualKids.next()), formalDecl = formalKids.next();

                if (decoration(actualDecl.getKid(2)) != decoration(formalDecl.getKid(2))) {
                    constraintError(ConstrainerErrors.ActualFormalTypeMismatch);
                }
            } catch (Exception e) {
                constraintError(ConstrainerErrors.NumberActualsFormalsDiffer);
            }
        }

        if (formalKids.hasNext()) {
            constraintError(ConstrainerErrors.NumberActualsFormalsDiffer);
        }

        return;
    }

    /**
     * Constrain the Decl tree:<br>
     * <ol>
     * <li>decorate to the corresponding intrinsic type tree,
     * <li>enter the
     * variable in the current scope so later variable references can
     * retrieve the information in this tree
     * </ol>
     */
    @Override
    public Object visitDeclTree(AST t) {
        AST idTree = t.getKid(2);
        enter(idTree, t);

        AST typeTree = getType(t.getKid(1));
        decorate(idTree, typeTree);

        return null;
    }

    /**
     * Constrain the <i>If</i> tree:<br>
     * check that the first kid is an expression that is a boolean type
     */
    @Override
    public Object visitIfTree(AST t) {
        if (t.getKid(1).accept(this) != boolTree) {
            constraintError(ConstrainerErrors.BadConditional);
        }

        t.getKid(2).accept(this);

        if (t.kidCount() == 3) {
            t.getKid(3).accept(this);
        }

        return null;
    }

    @Override
    public Object visitWhileTree(AST t) {
        if (t.getKid(1).accept(this) != boolTree) {
            constraintError(ConstrainerErrors.BadConditional);
        }

        t.getKid(2).accept(this);

        return null;
    }

    @Override
    public Object visitUnlessTree(AST t) {
        if (t.getKid(1).accept(this) != boolTree) {
            constraintError(ConstrainerErrors.BadConditional);
        }

        t.getKid(2).accept(this);

        return null;
    }

    /**
     * Constrain the Return tree:<br>
     * Check that the returned expression type matches the type indicated
     * in the function we're returning from
     */
    @Override
    public Object visitReturnTree(AST t) {
        if (functions.empty()) {
            constraintError(ConstrainerErrors.ReturnNotInFunction);
        }

        AST currentFunction = (functions.peek());
        decorate(t, currentFunction);

        AST returnType = decoration(currentFunction.getKid(1));
        if ((t.getKid(1).accept(this)) != returnType) {
            constraintError(ConstrainerErrors.BadReturnExpr);
        }

        return null;
    }

    /**
     * Constrain the Assign tree:<br>
     * be sure the types of the right-hand-side expression and variable
     * match; when we constrain an expression we'll return a reference
     * to the intrinsic type tree describing the type of the expression
     */
    @Override
    public Object visitAssignTree(AST t) {
        AST idTree = t.getKid(1), idDecl = lookup(idTree), typeTree;
        decorate(idTree, idDecl);

        typeTree = decoration(idDecl.getKid(2));

        // now check that the types of the expr and id are the same
        // visit the expr tree and get back its type
        Object exprType = t.getKid(2).accept(this);
        if (!exprType.equals(typeTree) && !(exprType instanceof String && typeTree.equals(stringTree))) {
            constraintError(ConstrainerErrors.BadAssignmentType);
        }
        return null;
    }

    @Override
    public Object visitIntTree(AST t) {
        decorate(t, intTree);
        return intTree;
    }

    @Override
    public Object visitStringTree(AST t) {
        decorate(t, stringTree);
        return stringTree;
    }

    @Override
    public Object visitHexTree(AST t) {
        decorate(t, hexTree);
        return hexTree;
    }

    @Override
    public Object visitIdTree(AST t) {
        AST decl = lookup(t);
        decorate(t, decl);
        return decoration(decl.getKid(2));
    }

    @Override
    public Object visitRelOpTree(AST t) {
        AST leftOp = t.getKid(1), rightOp = t.getKid(2);

        if ((AST) (leftOp.accept(this)) != (AST) (rightOp.accept(this))) {
            constraintError(ConstrainerErrors.TypeMismatchInExpr);
        }

        decorate(t, boolTree);
        return boolTree;
    }

    /**
     * Constrain the expression tree with an adding op at the root:<br>
     * e.g. t1 + t2<br>
     * check that the types of t1 and t2 match, if it's a plus tree
     * then the types must be a reference to the intTree
     * 
     * @return the type of the tree
     */
    @Override
    public Object visitAddOpTree(AST t) {
        AST leftOpType = (AST) (t.getKid(1).accept(this)), rightOpType = (AST) (t.getKid(2).accept(this));

        if (leftOpType != rightOpType) {
            constraintError(ConstrainerErrors.TypeMismatchInExpr);
        }

        decorate(t, leftOpType);

        return leftOpType;
    }

    @Override
    public Object visitMultOpTree(AST t) {
        return visitAddOpTree(t);
    }

    @Override
    public Object visitIntTypeTree(AST t) {
        return null;
    }

    @Override
    public Object visitBoolTypeTree(AST t) {
        return null;
    }

    @Override
    public Object visitStringTypeTree(AST t) {
        return null;
    }

    @Override
    public Object visitHexTypeTree(AST t) {
        return null;
    }

    @Override
    public Object visitFormalsTree(AST t) {
        return null;
    }

    @Override
    public Object visitActualArgsTree(AST t) {
        return null;
    }

    void constraintError(ConstrainerErrors err) {
        PrintVisitor v1 = new PrintVisitor();
        v1.visitProgramTree(t);

        System.out.println("****CONSTRAINER ERROR: " + err + "   ****");
        System.exit(1);

        return;
    }

}