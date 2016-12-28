package cahill.poc;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StatementHandler {

  //Don't support joins yet
  //Support only left to right clauses field operator value
  public static String handleClauseLanguage(String clauseLangauage) throws Exception {

    //TODO remove stop words
    //TODO convert dates
    //TODO validate fields
    //TODO handle quoting
    //TODO synonyms
    //TODO convert to SQL

    //remove punctuation and split on white space
    //TODO how to handle parenthesis (x > y or i = 1)
    clauseLangauage = clauseLangauage.replaceAll("[(){},;'!?%]|\\.$", "");
    String[] words = clauseLangauage.toLowerCase().split("\\s+");

    List<String> stopWords = new ArrayList<>(Arrays.asList("the", "with"));

    //First word should be the split word
    String splitWord = words[0];
    switch (splitWord) {
      //TODO handle things like "where value = 10 from field"
      case "from":
        clauseLangauage = clauseLangauage.replace(splitWord, "");
        String dateField = getDateField(clauseLangauage);
        if (dateField.isEmpty()) throw new Exception("No Date Field found in " + clauseLangauage);
        if (clauseLangauage.contains("to")) {
          //TODO handle defined range query
          //TODO check if there is a date
          String[] toSplits = clauseLangauage.split("to");
          String startDate = getUndefinedRange(toSplits[0]);
          String endDate = getUndefinedRange(toSplits[1]);
          //TODO check for simple numeric values

          //TODO handle alphabetic range queries

        } else {
          //handle range query within value
          //TODO change function name
          String date = getUndefinedRange(clauseLangauage);
          return dateField + " = '" + date + "'";
        }
        break;
      case "and": //allow fall through
      case "where":
        clauseLangauage = clauseLangauage.replace(splitWord, "");
        String cleanedClause = handleOperatorSynonyms(clauseLangauage);
        Operator operator = StatementHandler.containsOperator(clauseLangauage);
        Expression expression = new Expression(cleanedClause, operator, stopWords);
        return expression.toString();
    }

    return "Not Handled: " + clauseLangauage;
  }

  //TODO implement a function to get a default date field or parse for field
  private static String getDateField(String clauseLangauage) {
    return "dateField";
  }

  public static Operator containsOperator(String clauseLanguage) throws Exception {
    List<String> operator = new ArrayList<>(
            Arrays.asList("start", "end", "like", "=", ">", "<", ">=", "<="));

    List<String> operatorList = Arrays.stream(clauseLanguage.split(" "))
            .filter(operator::contains)
            .collect(Collectors.toList());

    if (operatorList.size() == 0) {
      throw new Exception("At least 1 operator expected.  The following are allowed: " + operator.toString() + ". Original Value: " + clauseLanguage);
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

  //Handle dates only; later on handle "from 100s area" type queries
  public static String getUndefinedRange(String value) throws ParseException {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    Annotation document = new Annotation(value);

    // run all Annotators on this text
    pipeline.annotate(document);

    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

    List<CoreLabel> tokenDates = new ArrayList<>();

    for(CoreMap sentence: sentences) {
      // traversing the words in the current sentence
      // a CoreLabel is a CoreMap with additional token-specific methods
      for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
        // this is the text of the token
        String word = token.get(CoreAnnotations.TextAnnotation.class);
        // this is the POS tag of the token
        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
        // this is the NER label of the token - can return if a term is a date
        String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
        if (ne.equalsIgnoreCase("date")) {
          tokenDates.add(token);
        }
        System.out.println();
      }
    }

    if (tokenDates.size() == 3) {
      //TODO figure out how to handle "First" or "twenty sixteen"
      List<String> dateFormatList = new ArrayList<>();
      boolean usedDay = false;
      boolean usedYear = false;
      for (CoreLabel coreLabel : tokenDates) {
      //tag nnp - string (proper noun), cd - number, rb - adverb (first)
        if (coreLabel.tag().equalsIgnoreCase("nnp")) {
          //Currently only supports Months as written
          if (coreLabel.originalText().length() <= 3) {
            dateFormatList.add("MMM");
          } else {
            dateFormatList.add("MMMM");
          }
        } else {
          if (coreLabel.originalText().length() == 4) {
            dateFormatList.add("yyyy");
          } else {
            //Use token order to determine - handle following orders
            //month  day year
            //year month day
            //day month year
            int stringToIntVal = NumberUtils.createInteger(coreLabel.originalText());
            if ( stringToIntVal > 31 || stringToIntVal <= 0) {
              dateFormatList.add("yy");
              usedYear = true;
            } else {
              //Day before year
              if (!usedDay) {
                dateFormatList.add("dd");
                usedDay = true;
              } else if (usedYear) {
                dateFormatList.add("yy");
              } else {
                dateFormatList.add("MM");
              }
            }
          }
        }
      }

      String format = StringUtils.join(dateFormatList, " ");

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
      Date date = simpleDateFormat.parse(
              StringUtils.join(
                      tokenDates.stream()
                              .map(CoreLabel::originalText)
                              .collect(Collectors.toList()), " " ));
      java.sql.Date sqlDate = new java.sql.Date(date.getTime());

      return sqlDate.toString();
    } else if (tokenDates.size() > 0) {
      //TODO if partial date, then turn date field into string and regex it
      //yyyy-mm-* or 2015-10-*
      return "";
    } else {
      //This is not a date, handle it
      return "";
    }
  }

  public static String handleOperatorSynonyms(String value) throws IOException {
    SynonymMap.Builder builder = new SynonymMap.Builder(true);
    addTo(builder, new String[]{"begin", "start", "first"}, new String[]{"start"});
    addTo(builder, new String[]{"end", "last", "finishes"}, new String[]{"end"});
    addTo(builder, new String[]{"has", "encompasses", "include", "contains"}, new String[]{"like"});
    addTo(builder, new String[]{"equals", "==", "same"}, new String[]{"="});
    addTo(builder, new String[]{"greater than", "greater"}, new String[]{">"});
    addTo(builder, new String[]{"less than", "less"}, new String[]{"<"});
    addTo(builder, new String[]{"less than equal to", "less than equal"}, new String[]{"<="});
    addTo(builder, new String[]{"greater than equal to", "greater than equal"}, new String[]{">="});

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

}
