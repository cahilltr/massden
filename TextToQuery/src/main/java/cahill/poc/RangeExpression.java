package cahill.poc;

import edu.stanford.nlp.ling.CoreLabel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class RangeExpression extends Expression {

  private final String queryField;
  private final String startValue;
  private final String endValue;

  //All start/end values that need quoted, should be quoted when setting the value
  public RangeExpression(String clause, Operator operator, List<String> stopWords) throws Exception {
    super(clause, operator, stopWords);

    this.queryField = findFieldValue(clause);
    if (queryField.isEmpty()) throw new Exception("No Date Field found in " + clause);
    String[] toSplits = clause.split("to");
    List<CoreLabel> allTokensFromFirstClause = getAllTokens(toSplits[0]);
    List<CoreLabel> firstClauseDateTokens = getDateTokensFromList(allTokensFromFirstClause);
    if (firstClauseDateTokens.size() > 0) { //Check if there is a date range
      this.startValue = "'" +  getDateFromTokens(firstClauseDateTokens) + "'";
      this.endValue = "'" + getDateFromTokens(getDateTokens(toSplits[1])) + "'";
    } else if (allTokensFromFirstClause.stream()
            .filter(
                    token -> token.tag().equalsIgnoreCase("cd"))
            .collect(Collectors.toList()).size() > 0) { //Check if it is a numeric range, tag 'cd' is a 'Cardinal Number'
      //Handle numeric range queries
      String startValue = getNumericValue(allTokensFromFirstClause);
      this.startValue = startValue == null ? "0" : startValue;
      String endValue = getNumericValue(getAllTokens(toSplits[1]));
      this.endValue = endValue == null ? "0" : endValue;
    } else {
      //Alphabetic range queries are handle between splits
      //Sort splits to put earliest alphabetical string as the start value
      Arrays.sort(toSplits);
      this.startValue = "'" + toSplits[0] + "'";
      this.endValue = "'" + toSplits[1] + "'";
    }
  }

  public String getQueryField() {
    return queryField;
  }

  public String getStartValue() {
    return startValue;
  }

  public String getEndValue() {
    return endValue;
  }

  @Override
  public String toString() {
    return this.queryField + " BETWEEN " + startValue + " AND " + endValue;
  }
}
