package cahill.poc;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Expression {

  private final String expression;
  private final Operator operator;
  private final List<String> stopWords;


  public Expression(String clause, Operator operator, List<String> stopWords) {
   this.expression = clause;
   this.operator = operator;
   this.stopWords = stopWords;
 }

  public String getExpression() {
    return expression;
  }

  public Operator getOperator() {
    return operator;
  }

  public List<String> getStopWords() { return this.stopWords; }

  //http://stackoverflow.com/questions/1889675/extract-nouns-from-text-java
  String findFieldValue(String expression) throws Exception {
    List<String> nonStopWordList = Arrays.stream(expression.split(" "))
            .filter(word -> !this.getStopWords().contains(word))
            .collect(Collectors.toList());

    if (nonStopWordList.size() == 0) {
      throw new Exception("No Allowable Fields Found: " + expression);
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

  String getNumericValue(List<CoreLabel> allTokens) {

    for (CoreLabel token : allTokens) {
      if (token.tag().equalsIgnoreCase("cd")) {
        if (NumberUtils.isNumber(token.originalText())) {
          return token.originalText();
        }
      }
    }
    return null;
  }

  protected List<CoreLabel> getDateTokens(String value) {
    List<CoreLabel> allTokens = getAllTokens(value);
    return getDateTokensFromList(allTokens);
  }

  protected List<CoreLabel> getDateTokensFromList(List<CoreLabel> allTokens) {

    List<CoreLabel> tokenDates = new ArrayList<>();
    for (CoreLabel token : allTokens) {
      // this is the NER label of the token - can return if a term is a date
      String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
      if (ne.equalsIgnoreCase("date")) {
        tokenDates.add(token);
      }
    }
    return tokenDates;
  }

  protected List<CoreMap> getDocumentSentences(String value) {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    Annotation document = new Annotation(value);

    // run all Annotators on this text
    pipeline.annotate(document);

    return document.get(CoreAnnotations.SentencesAnnotation.class);
  }

  protected List<CoreLabel> getAllTokens (String value) {
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
    return tokenDates;
  }

  //TODO use indexes to help determine ordering for month, day, year
  public static String getDateFromTokens(List<CoreLabel> tokenDates) throws ParseException {
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
  }
}
