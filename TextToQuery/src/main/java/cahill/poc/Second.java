package cahill.poc;

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
import java.util.*;
import java.util.stream.Collectors;

public class Second {

    public static final List<String> splitList = new ArrayList<>(Arrays.asList("from", "where", "and"));

    public static void main(String[] args) throws Exception {
        //Form:
        // What to return
        // from, where, and 'and'

        String text4 = "When the date is June 2016 through october 2016 or the name ends with Amazon, return all rows";

        //TOOD handle dates and queries like "from june to october"


        String text = "I want some values from june 2015 where the name contains Amazon";
        String text2 = "Get some values from june where the name starts with Amazon";
        String text5 = "Get some values from around june 2015 where the name starts with Amazon";
        String text6 = "Get some values from around june 2015 where the price >= 100.38";
        String text7 = "Get some values where the price >= 100.38 and age greater than 10";
        String text3 = "Return some values from june 2015 to june 2016 where the name starts with Amazon";

        System.out.println(getUndefinedRange("june 2015"));


//        String clauseSynonymsString = handleClauseSynonyms(text6);
//        List<String> splitterArray = splitter(clauseSynonymsString);
//
//        String startString = splitterArray.get(0);
//        String select = handleSelectLanguage(startString);
//        List<String> clauses = splitterArray.subList(1, splitterArray.size());
//
//        for (String clause : clauses) {
//            System.out.println(handleClauseLanguage(clause));
//        }
    }

    public static String generateQuery(String select, List<String> clauses) {
        return select + " " + StringUtils.join(clauses, " AND ");
    }

    public static String handleSelectLanguage(String selectLangauge) {

        //TODO remove stop words
        //TODO validate fields
        //TODO synonymns
        //TODO convert to SQL


        return "";
    }

    //Dont support joins yet
    //Support only left to right clauses field operator value
    public static String handleClauseLanguage(String clauseLangauage) throws Exception {

        //TODO remove stop words
        //TODO convert dates
        //TODO validate fields
        //TODO handle quoting
        //TODO synonymns
        //TODO convert to SQL

        //remove punctuation and split on white space
        //TODO how to handle parenthesis (x > y or i = 1)
        clauseLangauage = clauseLangauage.replaceAll("[(){},;'!?%]|\\.$", "");
        String[] words = clauseLangauage.toLowerCase().split("\\s+");

        List<String> stopWords = new ArrayList<>(Arrays.asList("the", "with"));

        //First word should be the split word
        String splitWord = words[0];
        switch (splitWord) {
            case "from":
                clauseLangauage = clauseLangauage.replace(splitWord, "");
                if (clauseLangauage.contains("to")) {
                    //handle defined range query
                } else {
                    //handle range query within value
                    String date = getUndefinedRange(clauseLangauage);

                }
                break;
            case "and": //allow fall through
            case "where":
                clauseLangauage = clauseLangauage.replace(splitWord, "");
                String[] operatorAndNewClause = containsOperator(clauseLangauage);
                if (operatorAndNewClause.length != 2) throw new Exception("Must have operator and cleaned Clause: " + Arrays.toString(operatorAndNewClause));
                String operator = operatorAndNewClause[0];
                clauseLangauage = operatorAndNewClause[1];

                //0 is left side, 1 is right side
                String[] sideSplit = clauseLangauage.split(operator);

                if (sideSplit.length != 2) {
                    throw new Exception("Malformed clause: " + clauseLangauage);
                }

                String leftSide = sideSplit[0];
                String rightSide = sideSplit[1];

                String fieldValue = findFieldValue(leftSide, stopWords);

                boolean isNumericalOperator = isOperatorNumerical(operator);

                String valueValue;
                if (isNumericalOperator) {
                    valueValue = handleNumericSide(rightSide);
                } else {
                    valueValue = handleStringSide(rightSide, operator);
                }
                return fieldValue + " " + operator + " " + valueValue;
        }

        return "Not Handled: " + clauseLangauage;
    }

    //Handle dates only; later on handle "from 100s area" type queries
    public static String getUndefinedRange(String value) {
        //check if date type
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date(value));
        Date date = calendar.getTime();

        java.sql.Date sqldate = new java.sql.Date(date.getTime());

        return sqldate.toString();
    }

    public static String handleStringSide(String sideOfOperator, String operator) throws Exception {

        String returnValue;
        switch (operator) {
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
                throw new Exception("Operator not found: " + operator);
        }
        return returnValue;
    }

    //return first word that is numeric
    public static String handleNumericSide(String sideOfOperator) throws Exception {

        String[] sideSplits = sideOfOperator.split(" ");
        for (String word : sideSplits) {
            if (NumberUtils.isNumber(word)) {
                return word;
            }
        }
        throw new Exception("No Numeric value found in: " + sideOfOperator);
    }

    public static boolean isOperatorNumerical(String operator) {
        //Are contains and like the same thing?
        List<String> stringOperators = new ArrayList<>(Arrays.asList("like", "starts", "ends"));
        return !stringOperators.contains(operator);
    }


    //http://stackoverflow.com/questions/1889675/extract-nouns-from-text-java
    public static String findFieldValue(String sideOfOperator, List<String> stopWordList) throws Exception {
        List<String> nonStopWordList = Arrays.stream(sideOfOperator.split(" "))
                .filter(word -> !stopWordList.contains(word))
                .collect(Collectors.toList());

        if (nonStopWordList.size() == 0) {
            throw new Exception("No Fields found: " + sideOfOperator);
        }

        for (String word : nonStopWordList) {
            if (isField(word)) {
                return word;
            }
        }

        throw new Exception("No word found as a field: " + nonStopWordList.toString());
    }


    //TODO metadata look up here
    public static boolean isField(String value) {
        return true;
    }

    //TODO handle Synonyms
    public static String[] containsOperator(String clauseLanguage) throws Exception {
        String cleanedClauseLanguage = handleOperatorSynonyms(clauseLanguage);
        List<String> operator = new ArrayList<>(Arrays.asList("start", "end", "like", "=", ">", "<", ">=", "<="));

        List<String> operatorList = Arrays.stream(cleanedClauseLanguage.split(" ")).filter(operator::contains).collect(Collectors.toList());

        if (operatorList.size() == 0) {
            throw new Exception("At least 1 operator expected.  The following are allowed: " + operator.toString() + ". Original Value: " + clauseLanguage);
        } else if (operatorList.size() > 1) {
            throw new Exception("More than 1 operator specified: " + operatorList.toString());
        }

        return new String[]{operatorList.get(0), cleanedClauseLanguage};
    }

    public static String handleClauseSynonyms(String value) throws IOException {
        SynonymMap.Builder builder = new SynonymMap.Builder(true);
        addTo(builder, new String[]{"where", "has"}, new String[]{"where"});
        addTo(builder, new String[]{"moreover", "furthermore", "also", "along with", "as well as"}, new String[]{"and"});
        addTo(builder, new String[]{"separating", "bounded by", "between", "range"}, new String[]{"from"});
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

    private static void addTo(SynonymMap.Builder builder, String[] from, String[] to) {
        for (String input : from) {
            for (String output : to) {
                builder.add(new CharsRef(input), new CharsRef(output), false);
            }
        }
    }

    public static List<String> splitter(String queryString) {

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

        do {
            int index = iterator.next();
            String subString = queryString.substring(startIndex, index - 1);
            startIndex = index;
            returnClausesArray.add(subString);
        } while (iterator.hasNext());

        returnClausesArray.add(queryString.substring(startIndex, queryString.length()));

        return returnClausesArray;
    }

}
