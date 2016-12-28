package cahill.poc;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TextToQuery implements Runnable {

  private final String originalText;
  private List<String> selectClauses;
  private List<String> conditionalClauses;
  private String sqlQuery;

  public TextToQuery(String text) {
    this.originalText = text;
  }

  //TODO handle Exceptions
  @Override
  public void run() {
    try {
      ParseText parseText = new ParseText(this.originalText);
      this.selectClauses = parseText.getSelectClauses();
      this.conditionalClauses = parseText.getConditionalClauses();

      String select = SelectStatement.handleSelectLanguage(this.selectClauses);
      List<String> clauseLanguageList = this.conditionalClauses
              .stream()
              .map(clause -> {
                String exception;
                try {
                  return StatementHandler.handleClauseLanguage(clause);
                } catch (Exception e) {
                  exception = e.getMessage();
                  e.printStackTrace();
                }
                return "Clause: " + clause + " contains the exception: " + exception;
              })
              .collect(Collectors.toList());

      this.sqlQuery = generateQuery(select, clauseLanguageList
              .stream()
              .filter(c -> !c.contains(" contains the exception: " ))
              .collect(Collectors.toList()));
    } catch (IOException e) {
      e.printStackTrace();
      this.sqlQuery = "Exception Occured: " + e.getMessage();
    }
  }

  private String generateQuery(String select, List<String> clauses) {
    return select + " " + StringUtils.join(clauses, " AND ");
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

  public String getSqlQuery() {
    return sqlQuery;
  }

}
