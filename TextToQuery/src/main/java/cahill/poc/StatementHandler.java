package cahill.poc;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class StatementHandler {

  public enum ExpressionType {
    RANGE, CONDITIONAL
  }

  //Don't support joins yet
  //Support only left to right clauses field operator value
  //TODO handle Aggregate function
  public static String handleClauseLanguage(String clauseLanguage) throws Exception {

    //TODO remove stop words
    //TODO convert dates
    //TODO validate fields
    //TODO handle quoting
    //TODO synonyms
    //TODO convert to SQL

    //remove punctuation and split on white space
    //TODO how to handle parenthesis (x > y or i = 1)
    clauseLanguage = clauseLanguage.replaceAll("[(){},;'!?%]|\\.$", "");
    String[] words = clauseLanguage.toLowerCase().split("\\s+");

    List<String> stopWords = new ArrayList<>(Arrays.asList("the", "with"));

    //First word should be the split word
    String splitWord = words[0];
    clauseLanguage = clauseLanguage.replace(splitWord, "");
    String cleanedClause = handleOperatorSynonyms(clauseLanguage);
    ExpressionType expressionType = determineExpressionType(cleanedClause);
    Operator operator = StatementHandler.containsOperator(cleanedClause);
    switch (expressionType) {
      case RANGE:
        RangeExpression rangeExpression = new RangeExpression(cleanedClause, operator, stopWords);
        return rangeExpression.toString();
      case CONDITIONAL:
        ConditionalExpression expression = new ConditionalExpression(cleanedClause, operator, stopWords);
        return expression.toString();
    }

    return "Not Handled: " + clauseLanguage;
  }

  public static ExpressionType determineExpressionType(String clause) {
    if (clause.contains("to")){
      return ExpressionType.RANGE;
    } else {
      return ExpressionType.CONDITIONAL;
    }
  }

  public static String handleOperatorSynonyms(String value) throws IOException {
    SynonymMap.Builder builder = new SynonymMap.Builder(true);
    addTo(builder, new String[]{"starts", "begin", "start", "first"}, new String[]{"starts"});
    addTo(builder, new String[]{"end", "last", "finishes"}, new String[]{"end"});
    addTo(builder, new String[]{"has", "encompasses", "include", "contains"}, new String[]{"like"});
    addTo(builder, new String[]{"equals", "==", "same", "is"}, new String[]{"="});
    addTo(builder, new String[]{"greater than", "greater"}, new String[]{">"});
    addTo(builder, new String[]{"less than", "less"}, new String[]{"<"});
    addTo(builder, new String[]{"less than equal to", "less than equal"}, new String[]{"<="});
    addTo(builder, new String[]{"greater than equal to", "greater than equal"}, new String[]{">="});
    addTo(builder, new String[]{"to", "between", "bounded by", "surround by", "enclosed by"}, new String[]{"to"});

    Analyzer analyzer = new WhitespaceAnalyzer();
    TokenStream ts0 = analyzer.tokenStream("name", new StringReader(value));

    SynonymFilter ts = new SynonymFilter(ts0, builder.build(), true);

    CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
    ts.reset();
    String newString = "";
    while (ts.incrementToken()) {
      newString += " " + termAtt.toString();
    }

    newString = newString.trim();
    analyzer.close();
    return newString;
  }

  public static Operator containsOperator(String clauseLanguage) throws Exception {
    List<String> operator = new ArrayList<>(
            Arrays.asList("starts", "end", "like", "=", ">", "<", ">=", "<=", "to"));

    List<String> operatorList = Arrays.stream(clauseLanguage.split(" "))
            .filter(operator::contains)
            .collect(Collectors.toList());

    if (operatorList.size() == 0) {
      throw new Exception("At least 1 operator expected.  The following are allowed: " + operator.toString()
              + ". Original Value: " + clauseLanguage);
    } else if (operatorList.size() > 1) {
      throw new Exception("More than 1 operator specified: " + operatorList.toString());
    }

    return new Operator(operatorList.get(0));
  }

  private static void addTo(SynonymMap.Builder builder, String[] from, String[] to) {
    for (String input : from) {
      for (String output : to) {
        builder.add(new CharsRef(input), new CharsRef(output), false);
      }
    }
  }
}
