package codegen;

/**
 *  The Block class is used to record the size of the current block
 *  Used in tandem with Frame
 */
public class Block {

  int size = 0;

  void change(int n) {
    size += n;
  }

  int getSize() {
    return size;
  }
}
