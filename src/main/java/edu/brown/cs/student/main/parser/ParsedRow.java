package edu.brown.cs.student.main.parser;

import java.util.List;

/**
 * ParsedRow is an example class of how some developer might want to take advantage of the generic
 * input to the parser class. ParsedRow is a type of object that gets created by the create method
 * in a different class, SecondRowHandler, so the dataset in MyParser would look like an ArrayList
 * of ParsedRows
 */
public class ParsedRow {

  public int index;
  public List<String> contents;

  /**
   * Constructor for ParsedRow, it takes in the rows index and the row itself and stores it.
   *
   * @param num index of the row
   * @param contents contents of the row
   */
  public ParsedRow(int num, List<String> contents) {
    this.index = num;
    this.contents = contents;
  }

  /**
   * toString method for ParsedRow.
   *
   * @return a string representation of this class
   */
  public String toString() {
    String retStr = "";
    retStr += this.index;
    for (String ele : this.contents) {
      retStr = retStr + ", " + ele;
    }
    return retStr;
  }
}
