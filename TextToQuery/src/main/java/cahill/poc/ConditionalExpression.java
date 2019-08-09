package cahill.poc;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class ConditionalExpression extends Expression{

  private final String fieldValue;
  private final String valueValue;

  public ConditionalExpression(String clause, Operator operator, List<String> stopWords) throws Exception {
    super(clause, operator, stopWords);

    //0 is left side, 1 is right side
    String[] sideSplit = clause.split(operator.getOperatorText());

    if (sideSplit.length != 2) {
      throw new Exception("Malformed clause: " + clause);
    }

    String leftSide = sideSplit[0];
    String rightSide = sideSplit[1];

    this.fieldValue = findFieldValue(leftSide);

    List<CoreLabel> allTokensFromFirstClause = getAllTokens(rightSide);
    List<CoreLabel> firstClauseDateTokens = getDateTokensFromList(allTokensFromFirstClause);

    if (firstClauseDateTokens.size() != 0) {
      this.valueValue = getDateValue(rightSide);
    } else if (operator.getIsOperatorNumerical()) {
      this.valueValue = handleNumericSide(rightSide);
    } else {
      this.valueValue = handleStringSide(rightSide, operator);
    }
  }

  public String getFieldValue() {
    return fieldValue;
  }

  public String getValueValue() {
    return valueValue;
  }

  @Override
  public String toString() {
    return this.fieldValue + " " +  this.getOperator().getOperatorText() + " " + this.valueValue;
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

  //Handle dates only; later on handle "from 100s area" type queries
  //TODO figure out how to handle "First" - this is called an ordinal number or "twenty sixteen"
  public String getDateValue(String value) throws java.text.ParseException {

    List<CoreMap> sentences = getDocumentSentences(value);

    List<CoreLabel> tokenDates = new ArrayList<>();

    for(CoreMap sentence: sentences) {
      // traversing the words in the current sentence
      // a CoreLabel is a CoreMap with additional token-specific methods
      for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
        // this is the NER label of the token - can return if a term is a date
        String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
        if (ne.equalsIgnoreCase("date")) {
          tokenDates.add(token);
        }
      }
    }

    if (tokenDates.size() == 3) {
      return "'" + getDateFromTokens(tokenDates) + "'";
    } else if (tokenDates.size() > 0) {
      //TODO if partial date, then turn date field into string and regex it
      // Make separate expression? Toss into string values?
      //yyyy-mm-* or 2015-10-*
      return "";
    } else {
      //This is not a date, handle it
      return "";
    }
  }
}
