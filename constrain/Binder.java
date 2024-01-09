package constrain;

import lexer.Symbol;

/**
 * Binder objects group 3 fields
 * 1. a value
 * 2. the next link in the chain of symbols in the current scope
 * 3. the next link of a previous Binder for the same identifier in a previous
 * scope
 */
class Binder {
  private Object value;
  // prior symbol in same scope
  private Symbol prevtop;
  // prior binder for same symbol
  // restore this when closing scope
  private Binder tail;

  Binder(Object v, Symbol p, Binder t) {
    value = v;
    prevtop = p;
    tail = t;
  }

  Object getValue() {
    return value;
  }

  Symbol getPrevtop() {
    return prevtop;
  }

  Binder getTail() {
    return tail;
  }
}
