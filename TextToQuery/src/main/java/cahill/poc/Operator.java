package cahill.poc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Operator {

  private final String operatorText;
  private boolean isOperatorNumerical;

  public Operator(String text) {
    operatorText = text;
    isOperatorNumerical(text);
  }

  public String getOperatorText() {
    return operatorText;
  }

  public boolean getIsOperatorNumerical() {
    //Are contains and like the same thing?
    return isOperatorNumerical;
  }

  private void isOperatorNumerical(String text) {
    List<String> stringOperators = new ArrayList<>(Arrays.asList("like", "starts", "ends", "to")); //To handles range queries
    this.isOperatorNumerical = !stringOperators.contains(this.operatorText);
  }
}
