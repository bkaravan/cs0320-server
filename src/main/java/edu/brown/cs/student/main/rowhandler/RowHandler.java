package edu.brown.cs.student.main.rowhandler;

import java.util.ArrayList;
import java.util.List;

/**
 * RowHandler is a class that is passed in MyParser in order to work with this particular
 * implementation of MySearcher. It implements the CreatorFromRow array using ArrayList of Strings,
 * so the dataset in MyParser becomes a list of list of string.
 */
public class RowHandler implements CreatorFromRow<ArrayList<String>> {

  /**
   * Create class take in a list of strings, strips every string of spaces, and returns an ArrayList
   * of the input List of Strings.
   *
   * @param row a list of strings that we are getting from parsing.
   * @return an ArrayList of stings from the input that is stripped of spaces.
   * @throws FactoryFailureException an exception that is thrown if any exceptions happen during row
   *     creation.
   */
  public ArrayList<String> create(List<String> row) throws FactoryFailureException {
    ArrayList<String> newRow = new ArrayList<>();
    for (String str : row) {
      if (str.isEmpty()) {
        throw new FactoryFailureException("Your row contains an empty entry", row);
      } else {
        newRow.add(str.strip());
      }
    }
    return newRow;
  }
}
