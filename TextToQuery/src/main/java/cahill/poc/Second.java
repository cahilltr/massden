package cahill.poc;

import org.apache.commons.lang.StringUtils;

import java.util.*;

public class Second {

    public static void main(String[] args) throws Exception {
        //Form:
        // What to return
        // from, where, and 'and'

        String text4 = "When the date is June 2016 through october 2016 or the name ends with Amazon, return all rows";

        String text8 = "For all values from table <name> ...";
        //TODO handle dates and queries like "from june to october"


        String text = "I want some values from june 2015 where the name contains Amazon";
        String text2 = "Get some values from june where the name starts with Amazon";
        String text5 = "Get some values from around june 2015 where the name starts with Amazon";
        String text6 = "Get some values from around june 2015 where the price >= 100.38";
        String text7 = "Get some values where the price >= 100.38 and age greater than 10";
        String text3 = "Return some values from june 2015 to june 2016 where the name starts with Amazon";
        String text9 = "Get some values from testTable from around june 1 2015 where the price >= 100.38";
        String text10 = "Get some values from testTable from around june first 2015 where the price >= 100.38";
        String text11 = "Get some values from testTable from around june first 2015 8:15 am where the price >= 100.38";


//        System.out.println(getUndefinedRange("june 2015"));

        ParseText parseText = new ParseText(text9);

        String select = SelectStatement.handleSelectLanguage(parseText.getSelectClauses());

        for (String clause : parseText.getConditionalClauses()) {
            System.out.println(StatementHandler.handleClauseLanguage(clause));
        }
    }

}
