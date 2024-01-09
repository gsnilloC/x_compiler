package codegen;

/**
 * Class Codes maintains bytecode related information:
 * 1. bytecode constants used for code generation
 * 2. bytecode strings used for printing bytecodes
 * 3. an indication on how each bytecode affects the
 *    runtime stack; we need to track changes to the
 *    stack to provide local variable offsets
 *    e.g. consider the following program
 *
 * program {
 *  // i is offset 0; j is offset 1
 *  int i boolean j
 *
 *  int factorial(int n) {
 *    // n is offset 0 in new frame
 *    if (n < 2) then {
 *      return 1
 *    } else {
 *      // i is offset 1 in new frame
 *      int i
 *      return n * factorial(n-1)
 *    }
 */

public class Codes {

  public static enum ByteCodes {
    HALT,
    POP,
    FALSEBRANCH,
    GOTO,
    STORE,
    LOAD,
    LIT,
    ARGS,
    CALL,
    RETURN,
    BOP,
    READ,
    WRITE,
    LABEL,
  }

  /*
    HALT         halt execution
    POP          pop n
    FALSEBRANCH  falsebranch <label>
    GOTO         goto <label>
    STORE        store n <varname>  -- n is frame offset
    LOAD         load n  <varname>  -- n is frame offset
    LIT          lit n              -- load n
    ARGS         args n             -- n = #args
    CALL         call <funcname>
    RETURN       return <funcname>
    BOP          bop <binary op>
    READ         read
    WRITE        write
    LABEL        label <label>
  */

  public static java.util.HashMap<ByteCodes, Integer> frameChange = new java.util.HashMap<ByteCodes, Integer>();
  public static final int UnknownChange = 99;

  /**
   *  Codes initializes the FrameChange array which records how
   *  execution of each bytecode instruction will affect the runtime stack
   *
   *  The following is a static block - it gets executed when this class is loaded
   */
  static {
    frameChange.put(ByteCodes.GOTO, 0);
    frameChange.put(ByteCodes.HALT, 0);

    // depends on how many popped:
    frameChange.put(ByteCodes.POP, UnknownChange);

    // pop conditional expr:
    frameChange.put(ByteCodes.FALSEBRANCH, -1);

    // pop value
    frameChange.put(ByteCodes.STORE, -1);

    // load new value
    frameChange.put(ByteCodes.LOAD, 1);

    // load literal value
    frameChange.put(ByteCodes.LIT, 1);

    // actual args
    frameChange.put(ByteCodes.ARGS, UnknownChange);

    // result of fct call is pushed
    frameChange.put(ByteCodes.CALL, 1);

    // pop return value
    frameChange.put(ByteCodes.RETURN, -1);

    // replace values with second level op top level
    frameChange.put(ByteCodes.BOP, -1);

    // read in new value
    frameChange.put(ByteCodes.READ, 1);

    // write value; leave on top
    frameChange.put(ByteCodes.WRITE, 0);

    // branch label
    frameChange.put(ByteCodes.LABEL, 0);
  }
}
