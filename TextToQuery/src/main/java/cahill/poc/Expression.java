package cahill.poc;


import org.apache.commons.lang.math.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Expression {

  private final String expression;
  private final String fieldValue;
  private final String valueValue;


  public Expression(String clause, Operator operator, List<String> stopWords) throws Exception {
    this.expression = clause;
    this.operator = operator;

    //0 is left side, 1 is right side
    String[] sideSplit = clause.split(operator.getOperatorText());

    if (sideSplit.length != 2) {
      throw new Exception("Malformed clause: " + clause);
    }

    String leftSide = sideSplit[0];
    String rightSide = sideSplit[1];

    this.fieldValue = findFieldValue(leftSide, stopWords);

    String valueValue;
    if (operator.getIsOperatorNumerical()) {
      this.valueValue = handleNumericSide(rightSide);
    } else {
      this.valueValue = handleStringSide(rightSide, operator);
    }
  }

  public String getExpression() {
    return expression;
  }

  public Operator getOperator() {
    return operator;
  }

  private final Operator operator;

  public String getFieldValue() {
    return fieldValue;
  }

  public String getValueValue() {
    return valueValue;
  }

  @Override
  public String toString() {
    return this.fieldValue + " " +  operator.getOperatorText() + " " + this.valueValue;
  }

  //return first word that is numeric
  private String handleNumericSide(String sideOfOperator) throws Exception {

    String[] sideSplits = sideOfOperator.split(" ");
    for (String word : sideSplits) {
      if (NumberUtils.isNumber(word)) {
        return word;
      }
    }
    throw new Exception("No Numeric value found in: " + sideOfOperator);
  }


  private String handleStringSide(String sideOfOperator, Operator operator) throws Exception {

    String returnValue;
    switch (operator.getOperatorText()) {
      case "like":
      case "contains":
        returnValue = "'%" + sideOfOperator.trim() + "%'";
        break;
      case "starts":
        returnValue = "'" + sideOfOperator.trim() + "%'";
        break;
      case "ends":
        returnValue = "'%" + sideOfOperator.trim() + "'";
        break;
      default:
        throw new Exception("StatementHandler not found: " + operator);
    }
    return returnValue;
  }

  //http://stackoverflow.com/questions/1889675/extract-nouns-from-text-java
  private String findFieldValue(String expression, List<String> stopWordList) throws Exception {
    List<String> nonStopWordList = Arrays.stream(expression.split(" "))
            .filter(word -> !stopWordList.contains(word))
            .collect(Collectors.toList());

    if (nonStopWordList.size() == 0) {
      throw new Exception("No Fields found: " + expression);
    }

    for (String word : nonStopWordList) {
      if (isField(word)) {
        return word;
      }
    }

    throw new Exception("No word found as a field: " + nonStopWordList.toString());
  }

  //TODO metadata look up here
  private boolean isField(String value) {
    return true;
  }
}
