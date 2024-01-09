package codegen;

import ast.*;
import constrain.*;
import java.util.*;
import visitor.*;

/**
* The Codegen class will walk the AST, determine and set variable
* offsets and generate the bytecodes
*/
public class Codegen extends ASTVisitor {
  
  AST t;
  /**
  * used for tracking the frame sizes;
  * when we start generating code for a
  * function we'll push a new entry on the
  * stack with init size zero
  */
  Stack<Frame> frameSizes;
  
  // program will contain the generated bytecodes
  Program program;
  // used for creating new, unique labels
  int labelNum;
  
  /**
  * Create a new code generator based on the given AST
  * 
  * @param t is the AST that will be visited
  */
  public Codegen(AST t) {
    this.t = t;
    program = new Program();
    frameSizes = new Stack<Frame>();
    labelNum = 0;
  }
  
  /**
  * visit all the nodes in the AST/gen bytecodes
  */
  public Program execute() {
    t.accept(this);
    
    return program;
  }
  
  Frame topFrame() {
    if (frameSizes.empty()) {
      System.out.println("frames empty");
    }
    
    return (Frame) frameSizes.peek();
  }
  
  /**
  * open a new frame - we're generating codes for
  * a function declaration
  */
  void openFrame() {
    frameSizes.push(new Frame());
  }
  
  /**
  * open a new block - store local variables
  */
  void openBlock() {
    topFrame().openBlock();
  }
  
  /**
  * close the current block (and pop the local
  * variables off the runtime stack
  */
  void closeBlock() {
    topFrame().closeBlock();
  }
  
  void closeFrame() {
    frameSizes.pop();
  }
  
  /**
  * change the size of the current frame by the
  * given amount
  */
  void changeFrame(int n) {
    topFrame().change(n);
  }
  
  /**
  * return the current frame size
  */
  int frameSize() {
    return topFrame().getSize();
  }
  
  int getBlockSize() {
    return topFrame().getBlockSize();
  }
  
  /**
  * we'll need to create new labels for the bytecode program
  * e.g. the following is legal despite 2 functions with the same name
  *
  * int f(int n) {...
    * {
      * int f() {...
        * x = f()
        * }
        * } ...
        *
        * y = f(5)
        * }
        *
        * we don't want to generate the label f for the start of both functions
        * instead, we'll generate, e.g., f<<0>> and f<<1>>
        *
        * create a new label from label
        */
        String newLabel(String label) {
          ++labelNum;
          return label + "<<" + labelNum + ">>";
        }
        
        void storeop(Code code) {
          // System.out.println(
          // String.format(
          // "storeop: %s fs: %d bs: %d",
          // code.toString(),
          // top.getSize(),
          // top.getBlockSize()
          // )
          // );
          
          Codes.ByteCodes bytecode = code.getBytecode();
          int change = Codes.frameChange.get(bytecode);
          program.storeop(code);
          
          if (change == Codes.UnknownChange) {
            // pop n; args n
            changeFrame(-((NumOpcode) code).getNum());
          } else {
            changeFrame(change);
          }
        }
        
        /**
        * generate codes for read/write functions so they're treated
        * as any other function
        */
        void genIntrinsicCodes() {
          String readLabel = "Read", writeLabel = "Write";
          AST readTree = Constrainer.readTree, writeTree = Constrainer.writeTree;
          
          readTree.setLabel(readLabel);
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, readLabel));
          storeop(new Code(Codes.ByteCodes.READ));
          storeop(new Code(Codes.ByteCodes.RETURN));
          
          writeTree.setLabel(writeLabel);
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, writeLabel));
          String formal = ((IdTree) (writeTree.getKid(3).getKid(1).getKid(2))).getSymbol()
          .toString();
          storeop(new VarOpcode(Codes.ByteCodes.LOAD, 0, formal));
          // write has one actual arg - in frame offset 0
          storeop(new Code(Codes.ByteCodes.WRITE));
          storeop(new Code(Codes.ByteCodes.RETURN));
        }
        
        /**
        * visit the given program AST:
        *
        * GOTO start -- branch around codes for the intrinsics
        * <generate codes for the intrinsic trees (read/write)>
        * LABEL start
        * <generate codes for the BLOCK tree>
        * HALT
        *
        * @param t the program tree to visit
        * @return null - we're a visitor so must return a value
        *         but the code generator doesn't need any specific value
        */
        @Override
        public Object visitProgramTree(AST t) {
          String startLabel = newLabel("start");
          openFrame();
          
          // branch over intrinsic bytecodes
          storeop(new LabelOpcode(Codes.ByteCodes.GOTO, startLabel));
          genIntrinsicCodes();
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, startLabel));
          
          t.getKid(1).accept(this);
          
          storeop(new Code(Codes.ByteCodes.HALT));
          closeFrame();
          
          return null;
        }
        
        /**
        * Generate codes for the Block tree:
        *
        * <codes for the decls and the statements in the block>
        * POP n -- n is the number of local variables; pop them
        */
        @Override
        public Object visitBlockTree(AST t) {
          // System.out.println("visitBlockTree");
          openBlock();
          
          visitKids(t);
          storeop(new NumOpcode(Codes.ByteCodes.POP, getBlockSize()));
          
          // remove any local variables from runtime stack
          closeBlock();
          
          return null;
        }
        
        @Override
        public Object visitSelectBlockTree(AST t) {
          // System.out.println("visitSelectBlockTree");
          openBlock();
          
          for (int i = 1; i < t.getNodeNum(); i++) {
            t.getKid(i).accept(this);
          }
          
          storeop(new NumOpcode(Codes.ByteCodes.POP, getBlockSize()));
          
          // remove any local variables from runtime stack
          closeBlock();
          
          return null;
        }
        
        @Override
        public Object visitSelectorTree(AST t) {
          AST expr = t.getKid(0);
          
          expr.accept(this);
          
          String blockLabel = newLabel("block");
          String endLabel = newLabel("end");
          
          storeop(new LabelOpcode(Codes.ByteCodes.FALSEBRANCH, endLabel));
          
          AST block = t.getKid(1);
          block.accept(this);
          
          storeop(new LabelOpcode(Codes.ByteCodes.GOTO, endLabel));
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, blockLabel));
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, endLabel));
          
          return null;
        }
        
        
        
        /**
        * Generate codes for the function declaration; we'll also record
        * the frame offsets for the formal parameters
        *
        * GOTO continue -- branch around codes for the function
        * LABEL functionLabel
        * <generate codes for the function body>
        * LIT 0
        * RETURN function
        * LABEL continue
        *
        */
        @Override
        public Object visitFunctionDeclTree(AST t) {
          // System.out.println("visitFunctionDeclTree");
          AST name = t.getKid(2), formals = t.getKid(3), block = t.getKid(4);
          
          String funcName = ((IdTree) name).getSymbol().toString();
          String funcLabel = newLabel(funcName);
          t.setLabel(funcLabel);
          
          String continueLabel = newLabel("continue");
          storeop(new LabelOpcode(Codes.ByteCodes.GOTO, continueLabel));
          
          // track Frame changes within function
          openFrame();
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, funcLabel));
          
          // now record the frame offsets for the formals
          for (AST decl : formals.getKids()) {
            IdTree id = (IdTree) (decl.getKid(2));
            id.setFrameOffset(frameSize());
            decl.setLabel(id.getSymbol().toString());
            
            // ensure frame size includes space for variables
            changeFrame(1);
          }
          
          block.accept(this);
          
          // emit gratis return in case user didn't provide their own return
          storeop(new VarOpcode(Codes.ByteCodes.LIT, 0, "   GRATIS-RETURN-VALUE"));
          storeop(new LabelOpcode(Codes.ByteCodes.RETURN, funcLabel));
          
          closeFrame();
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, continueLabel));
          
          return null;
        }
        
        /**
        * Generate codes for the call tree:
        *
        * <Codes to evaluate the arguments for the function>
        * ARGS n -- where n is the number of args
        * CALL functionName
        */
        @Override
        public Object visitCallTree(AST t) {
          // System.out.println("visitCallTree");
          String funcName = ((IdTree) t.getKid(1)).getDecoration().getLabel();
          int numArgs = t.kidCount() - 1;
          
          for (int kid = 2; kid <= t.kidCount(); kid++) {
            t.getKid(kid).accept(this);
          }
          
          storeop(new NumOpcode(Codes.ByteCodes.ARGS, numArgs));
          // used to set up new frame
          storeop(new LabelOpcode(Codes.ByteCodes.CALL, funcName));
          
          return null;
        }
        
        /**
        * Generate codes for the Decl tree:
        *
        * LIT 0 -- 0 is the initial value for the variable
        *
        * record the frame offset of this variable for future references
        */
        @Override
        public Object visitDeclTree(AST t) {
          // System.out.println("visitDeclTree");
          IdTree id = (IdTree) t.getKid(2);
          String idLabel = id.getSymbol().toString();
          
          // set label in declaration node
          t.setLabel(idLabel);
          
          // reserve space in frame for new variable; init to 0
          id.setFrameOffset(frameSize());
          storeop(new VarOpcode(Codes.ByteCodes.LIT, 0, idLabel));
          
          return null;
        }
        
        @Override
        public Object visitIntTypeTree(AST t) {
          // System.out.println("visitIntTypeTree");
          return null;
        }
        
        @Override
        public Object visitBoolTypeTree(AST t) {
          // System.out.println("visitBoolTypeTree");
          return null;
        }
        
        @Override
        public Object visitFormalsTree(AST t) {
          // System.out.println("visitFormalsTree");
          return null;
        }
        
        @Override
        public Object visitActualArgsTree(AST t) {
          // System.out.println("visitActualArgsTree");
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
        
        /**
        * Generate codes for the If tree:
        *
        * <generate codes for the conditional tree>
        * FALSEBRANCH elseLabel
        * <generate codes for the then tree - 2nd kid>
        * GOTO continue
        * LABEL elseLabel
        * <generate codes for the else tree - 3rd kid>
        * LABEL continue
        */
        @Override
        public Object visitIfTree(AST t) {
          String elseLabel = newLabel("else"), continueLabel = newLabel("continue");
          
          boolean hasElse = t.getNodeNum() == 4;
          
          // generate code for conditional expr
          t.getKid(1).accept(this);
          storeop(new LabelOpcode(Codes.ByteCodes.FALSEBRANCH, hasElse ? elseLabel : continueLabel));
          
          t.getKid(2).accept(this);
          storeop(new LabelOpcode(Codes.ByteCodes.GOTO, continueLabel));
          
          // generate code for "else" block, if there is one
          if (hasElse) {
            storeop(new LabelOpcode(Codes.ByteCodes.LABEL, elseLabel));
            t.getKid(3).accept(this);
          }
          
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, continueLabel));
          
          return null;
        }
        
        
        @Override
        public Object visitUnlessTree(AST t) {
          // System.out.println("visitIfTree");
          String elseLabel = newLabel("else"), continueLabel = newLabel("continue");
          
          // gen code for conditional expr
          t.getKid(1).accept(this);
          storeop(new LabelOpcode(Codes.ByteCodes.FALSEBRANCH, elseLabel));
          
          t.getKid(2).accept(this);
          storeop(new LabelOpcode(Codes.ByteCodes.GOTO, continueLabel));
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, elseLabel));
          
          t.getKid(3).accept(this);
          
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, continueLabel));
          
          return null;
        }
        
        /**
        * Generate codes for the While tree:
        *
        * LABEL while
        * <generate codes for the conditional>
        * FALSEBRANCH continue
        * <generate codes for the body of the while>
        * GOTO while
        * LABEL continue
        */
        @Override
        public Object visitWhileTree(AST t) {
          // System.out.println("visitWhileTree");
          String continueLabel = newLabel("continue"), whileLabel = newLabel("while");
          
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, whileLabel));
          t.getKid(1).accept(this);
          
          storeop(new LabelOpcode(Codes.ByteCodes.FALSEBRANCH, continueLabel));
          t.getKid(2).accept(this);
          
          storeop(new LabelOpcode(Codes.ByteCodes.GOTO, whileLabel));
          storeop(new LabelOpcode(Codes.ByteCodes.LABEL, continueLabel));
          
          return null;
        }
        
        /**
        * Generate codes for the return tree:
        *
        * <generate codes for the expression that will be returned>
        * RETURN <name-of-function>
        */
        @Override
        public Object visitReturnTree(AST t) {
          // System.out.println("visitReturnTree");
          t.getKid(1).accept(this);
          
          AST fct = t.getDecoration();
          storeop(new LabelOpcode(Codes.ByteCodes.RETURN, fct.getLabel()));
          
          return null;
        }
        
        /**
        * Generate codes for the Assign tree:
        *
        * <generate codes for the right-hand-side expression>
        * STORE offset-of-variable name-of-variable
        */
        @Override
        public Object visitAssignTree(AST t) {
          // System.out.println("visitAssignTree");
          IdTree id = (IdTree) t.getKid(1);
          String vname = id.getSymbol().toString();
          int addr = ((IdTree) (id.getDecoration().getKid(2))).getFrameOffset();
          
          t.getKid(2).accept(this);
          
          storeop(new VarOpcode(Codes.ByteCodes.STORE, addr, vname));
          
          return null;
        }
        
        /**
        * Load a literal value:
        * LIT n -- n is the value
        */
        @Override
        public Object visitIntTree(AST t) {
          // System.out.println("visitIntTree");
          int num = Integer.parseInt(((IntTree) t).getSymbol().toString());
          
          storeop(new NumOpcode(Codes.ByteCodes.LIT, num));
          
          return null;
        }
        
        @Override
        public Object visitStringTree(AST t) {
          String str = ((StringTree) t).getSymbol().toString();
          storeop(new StringOpcode(Codes.ByteCodes.LIT, str));
          return null;
        }
        
        
        
        @Override
        public Object visitHexTree(AST t) {
          String hexString = ((HexTree) t).getSymbol().toString();
          int num = Integer.parseInt(hexString.substring(2), 16);
          storeop(new NumOpcode(Codes.ByteCodes.LIT, num));
          return null;
        }
        
        
        /**
        * Load a variable:
        * LOAD offset -- load variable using the offset recorded in the AST
        */
        @Override
        public Object visitIdTree(AST t) {
          // System.out.println("visitIdTree");
          AST decl = t.getDecoration();
          int addr = ((IdTree) (decl.getKid(2))).getFrameOffset();
          String vname = ((IdTree) t).getSymbol().toString();
          
          storeop(new VarOpcode(Codes.ByteCodes.LOAD, addr, vname));
          
          return null;
        }
        
        /**
        * Generate codes for the relational op tree e.g. t1 == t2
        *
        * <generate codes for t1>
        * <generate codes for t2>
        * BOP op -- op is the indicated relational op
        */
        @Override
        public Object visitRelOpTree(AST t) {
          // System.out.println("visitRelOpTree");
          String op = ((RelOpTree) t).getSymbol().toString();
          
          t.getKid(1).accept(this);
          t.getKid(2).accept(this);
          
          storeop(new LabelOpcode(Codes.ByteCodes.BOP, op));
          
          return null;
        }
        
        /**
        * Generate codes for the adding op tree e.g. t1 + t2
        *
        * <generate codes for t1>
        * <generate codes for t2>
        * BOP op -- op is the indicated adding op
        */
        @Override
        public Object visitAddOpTree(AST t) {
          // System.out.println("visitAddOpTree");
          String op = ((AddOpTree) t).getSymbol().toString();
          
          t.getKid(1).accept(this);
          t.getKid(2).accept(this);
          
          storeop(new LabelOpcode(Codes.ByteCodes.BOP, op));
          
          return null;
        }
        
        /**
        * Generate codes for the multiplying op tree e.g. t1 * t2
        *
        * <generate codes for t1>
        * <generate codes for t2>
        * BOP op -- op is the indicated multiplying op
        */
        @Override
        public Object visitMultOpTree(AST t) {
          // System.out.println("visitMultOpTree");
          String op = ((MultOpTree) t).getSymbol().toString();
          
          t.getKid(1).accept(this);
          t.getKid(2).accept(this);
          
          storeop(new LabelOpcode(Codes.ByteCodes.BOP, op));
          
          return null;
        } 
        
        @Override
        public Object visitSelectTree(AST t) {
          
          for (int i = 1; i < t.getNodeNum(); i++) {
            AST selector = t.getKid(i);
            
            selector.getKid(0).accept(this);
            
            String endLabel = newLabel("end");
            storeop(new LabelOpcode(Codes.ByteCodes.POP, endLabel));
            selector.getKid(1).accept(this);
            storeop(new LabelOpcode(Codes.ByteCodes.GOTO, endLabel));
            storeop(new LabelOpcode(Codes.ByteCodes.LABEL, endLabel));
          }
          
          return null;
        }    
      }
      