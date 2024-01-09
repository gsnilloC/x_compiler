package codegen;

import java.util.Stack;

/**
 *  The Frame class is used for tracking the frame size as we generated
 *  bytecodes; we need the frame size to determine offsets for variables
 */
public class Frame {

  // size of current frame
  private int size = 0;
  private Stack<Block> blockSizes = new Stack<Block>();

  // If we are embedded 3 blocks deep in the current frame
  // then the blockSizes stack will have 3 items in it,
  // each recording the size of the associated block
  // e.g. consider the following source program segment
  //     int f(int n,int p) {  <- bottom block has formals
  //        int i;             <- next block has i
  //        { int k; int l; int m <- next block has k, l, and m
  //         ...
  //        the blockSizes stack has 2,1,3 with 3 at the top
  //        the framesize is 6 - the sum of all the block sizes

  public Frame() {
    openBlock();
  }

  int getSize() {
    return size;
  }

  void openBlock() {
    blockSizes.push(new Block());
  }

  int closeBlock() {
    int bsize = getBlockSize();

    // all items in the current block are gone:
    size -= bsize;

    // so decrease the frame size
    blockSizes.pop();

    return bsize;
  }

  Block topBlock() {
    return (Block) blockSizes.peek();
  }

  // change current block size; framesize
  void change(int n) {
    size += n;
    topBlock().change(n);
  }

  int getBlockSize() {
    return topBlock().getSize();
  }
}
