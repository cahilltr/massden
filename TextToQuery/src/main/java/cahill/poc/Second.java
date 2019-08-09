package cahill.poc;

import java.io.IOException;

public class Second {

    public static void main(String[] args) throws IOException {
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
        //Specific date without from keyword
        String text15 = "Get some values from testTable where date = june 1 2015 where the price >= 100.38";
        String text16 = "Get some values from testTable where date = june first 2015 where the price >= 100.38";
        String text17 = "Get some values from testTable where date = june first 2015 8:15 am where the price >= 100.38";
        //Specific dates with from keyword
        String text12 = "Get some values from testTable from june 1 2015 where the price >= 100.38";
        String text13 = "Get some values from testTable from june first 2015 where the price >= 100.38";
        String text14 = "Get some values from testTable from june first 2015 8:15 am where the price >= 100.38";
        //TODO work to handle proximity queries like below
        String text9 = "Get some values from testTable from around june 1 2015 where the price >= 100.38";
        String text10 = "Get some values from testTable from around june first 2015 where the price >= 100.38";
        String text11 = "Get some values from testTable from around june first 2015 8:15 am where the price >= 100.38";

        //Range Queries
        //TODO fix me - currently splits "and date is from june ..." into 2 clauses, "date is" and "from june..."
        String text18 = "Return values where price >= 10 and date is from june 1 2015 to september 20 2016";

        ParseText parseText = new ParseText(text2);

        String select = SelectStatement.handleSelectLanguage(parseText.getSelectClauses());

        System.out.println(select);
        for (String clause : parseText.getConditionalClauses()) {
            try {
                System.out.println(StatementHandler.handleClauseLanguage(clause));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
