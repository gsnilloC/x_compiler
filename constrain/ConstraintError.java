package constrain;

import java.util.Iterator;
import java.util.List;

public class ConstraintError extends Exception {
  private List<Constrainer.ConstrainerErrors> errors;

  public ConstraintError(List<Constrainer.ConstrainerErrors> errors) {
    this.errors = errors;
  }

  @Override
  public String getMessage() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Constrainer error(s): ");

    for (Iterator<Constrainer.ConstrainerErrors> iterator = errors.iterator(); iterator.hasNext();) {
      buffer.append(iterator.next() + ", ");
    }

    buffer.deleteCharAt(buffer.length() - 1);
    buffer.deleteCharAt(buffer.length() - 1);

    return buffer.toString();
  }
}
