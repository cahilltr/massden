package cahill.poc;

import org.apache.commons.lang.StringUtils;
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

public class ParseText {

  private static final List<String> splitList = new ArrayList<>(Arrays.asList("from", "where", "and"));

  private final String originalText;
  private final List<String> selectClauses;
  private final List<String> conditionalClauses;

  public ParseText(String text) throws IOException {
    this.originalText = text;
    String clauseSynonymsString = handleClauseSynonyms(this.originalText);
    List<String> splitterArray = splitter(clauseSynonymsString);


    List<Integer> selectClauseIndexes = determineSelectClauses(splitterArray);
    this.selectClauses = selectClauseIndexes.stream()
            .map(splitterArray::get)
            .collect(Collectors.toList());

    this.conditionalClauses = splitterArray.stream()
            .filter(c -> !selectClauses.contains(c))
            .collect(Collectors.toList());
  }

  public static List<String> getSplitList() {
    return splitList;
  }

  public String getOriginalText() {
    return originalText;
  }

  public List<String> getSelectClauses() {
    return selectClauses;
  }

  public List<String> getConditionalClauses() {
    return conditionalClauses;
  }

  //TODO make me much better
  private List<String> splitter(String queryString) {

    Set<Integer> splitIndexes = new TreeSet<>();
    for (String splitString : splitList) {
      int index = queryString.indexOf(splitString);
      while (index >= 0) {
        splitIndexes.add(index);
        index = queryString.indexOf(splitString, index + 1);
      }
    }
    List<String> returnClausesArray = new ArrayList<>(splitIndexes.size());
    int startIndex = 0;
    Iterator<Integer> iterator = splitIndexes.iterator();

    String lastSplit = "";
    boolean handledFirstTerm = false;
    do {
      int index = iterator.next();
      String subString = queryString.substring(startIndex, index - 1);
      startIndex = index;
      if (subString.split(" ").length <= 3) { //split word, value, operator or other
        if (!handledFirstTerm) {
          returnClausesArray.add(subString);
          handledFirstTerm = true;
        } else {
          lastSplit = subString;
        }
      } else {
        if (!lastSplit.isEmpty()) {
          subString = lastSplit + " " + subString;
          lastSplit = StringUtils.EMPTY;
        }
        returnClausesArray.add(subString);
      }
    } while (iterator.hasNext());

    returnClausesArray.add((lastSplit.isEmpty() ? "" : lastSplit + " " ) + queryString.substring(startIndex, queryString.length()));

    return returnClausesArray;
  }

  private List<Integer> determineSelectClauses(List<String> allClauses) {
    //Anything not containing any splitList String is select clause or if it contains "from" then double check from clauses

    List<Integer> selectClauses = allClauses.stream()
            .filter(clause ->
                    splitList.stream()
                            .filter(clause::contains)
                            .count() <= 0)
            .map(allClauses::indexOf)
            .collect(Collectors.toList());

    selectClauses.addAll(allClauses.stream()
            .filter(clause -> clause.contains("from"))
            .filter(this::isFromTermSelectClause)
            .map(allClauses::indexOf)
            .collect(Collectors.toList()));

    return selectClauses;
  }

  //Check if any term in the clause is an actual table
  private boolean isFromTermSelectClause(String clause) {
    //If clause contains "table" or "database", then it is apart of the select clause
    String lcClause = clause.toLowerCase();

    return (lcClause.contains("table") || lcClause.contains("database"));
  }

  private String handleClauseSynonyms(String value) throws IOException {
    SynonymMap.Builder builder = new SynonymMap.Builder(true);
    addTo(builder, new String[]{"where", "has"}, new String[]{"where"});
    addTo(builder, new String[]{"moreover", "furthermore", "also", "along with", "as well as"}, new String[]{"and"});
//    addTo(builder, new String[]{"separating", "bounded by", "between", "range"}, new String[]{"from"});
    addTo(builder, new String[]{"to", "until", "till", "up to", "stopping at", "extending to"}, new String[]{"to"});

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

  private void addTo(SynonymMap.Builder builder, String[] from, String[] to) {
    for (String input : from) {
      for (String output : to) {
        builder.add(new CharsRef(input), new CharsRef(output), false);
      }
    }
  }

}
