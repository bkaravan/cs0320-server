package edu.brown.cs.student.main.rowhandler;

import edu.brown.cs.student.main.parser.ParsedRow;
import java.util.List;

/**
 * SecondRowHandler is an example of a different implementation of the CreatorFromRow interface that
 * uses the ParsedRow custom object.
 */
public class SecondRowHandler implements CreatorFromRow<ParsedRow> {

  private int count;

  /** The constructor initializes the count that helps to keep track of the array indices. */
  public SecondRowHandler() {
    this.count = 0;
  }

  /**
   * Create method creates a ParsedRow objects and returns it.
   *
   * @param row a list of strings from the parser
   * @return ParsedRow from the input row
   * @throws FactoryFailureException when something fails during RowMaking
   */
  public ParsedRow create(List<String> row) throws FactoryFailureException {
    ParsedRow retRow = new ParsedRow(this.count, row);
    this.count++;
    return retRow;
  }
}
