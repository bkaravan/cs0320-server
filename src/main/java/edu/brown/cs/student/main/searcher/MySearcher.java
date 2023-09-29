package edu.brown.cs.student.main.searcher;

import java.util.ArrayList;
import java.util.List;

/**
 * MySearcher is a class that is responsible for searching through a given dataset of parsed data.
 * In this implementation, the dataset is a list of list of strings, so the worst case scenario for
 * the search of the word would be O(M * N), where M is the number of rows and N is the size of one
 * row. MySearcher creates a List of every row that has a match with the search word and stores it
 * as a field
 */
public class MySearcher {

  private ArrayList<List<String>> found = new ArrayList<>();

  private final List<List<String>> dataset;
  private int narrowIndex;
  private final String narrow;
  private final boolean isHeader;
  private int startIndex;

  /**
   * Constructor for the MySearcher class.
   *
   * @param dataset dataset from MyParser
   * @param header boolean value to indicate whether the dataset has a header
   * @param key a string that narrows down the search, if provided by user. Defaults to NULL in main
   */
  public MySearcher(List<List<String>> dataset, boolean header, String key) {
    this.dataset = dataset;
    this.narrow = key;
    this.isHeader = header;
    this.setUp();
  }

  /**
   * This method is in a way a helper method for the constructor. Based on the parameters that we
   * take in, we change the startIndex and narrowIndex fields. If we have a header, we want to start
   * looking for our matches starting with our second row in the dataset, hence the startIndex. The
   * switch case of this method is responsible for determining whether a user indicated that the
   * search is done through a name of the column, an index, or if it's a search of the whole dataset
   */
  private void setUp() {
    if (this.isHeader) {
      this.startIndex = 1;
    } else {
      this.startIndex = 0;
    }
    String match = this.narrow.substring(0, 4).toLowerCase();
    switch (match) {
      case "ind:" -> {
        try {
          this.narrowIndex = Integer.parseInt(this.narrow.substring(4).strip());
          if (this.narrowIndex >= this.dataset.get(0).size()) {
            System.err.println("Please make sure that you provide a valid Index");
            throw new IllegalArgumentException("Please provide a valid index");
          }
        } catch (NumberFormatException e) {
          System.err.println("Please make sure to use an integer after Ind: ");
          throw new NumberFormatException("Please make sure to use an integer after Ind:");
        }
      }
      case "nam:" -> {
        if (this.isHeader) {
          this.narrowIndex = this.dataset.get(0).indexOf(this.narrow.substring(4).strip());
        } else {
          System.err.println("Please only search by column name when the header row is present");
          throw new IllegalArgumentException("Searching by column name without header row");
        }
      }
      default -> this.narrowIndex = -1;
    }
  }

  /**
   * indexSearch is only called when there is correct column index we are interested in (which is
   * either a match with the colum name or index itself). IT ONLY LOOKS FOR ROW ENTRIES OF THAT
   * INDEX
   *
   * @param toFind the search word
   */
  private void indexSearch(String toFind) {
    for (int i = this.startIndex; i < this.dataset.size(); i++) {
      List<String> row = this.dataset.get(i);
      if (row.get(this.narrowIndex).contains(toFind)) {
        this.found.add(row);
      }
    }
  }

  /**
   * allSearch loops through the whole dataset and looks for any matches with the search word.
   *
   * @param toFind the search word
   */
  private void allSearch(String toFind) {
    for (int i = this.startIndex; i < this.dataset.size(); i++) {
      List<String> row = this.dataset.get(i);
      if (row.contains(toFind)) {
        this.found.add(row);
      }
    }
  }

  /**
   * public method that is called from the outside of this class, here it decides which search
   * method to call based on the parameter narrowIndex.
   *
   * @param toFind the word we are looking for in the dataset
   */
  public void findRows(String toFind) {
    this.found = new ArrayList<>();
    if (this.narrowIndex == -1) {
      this.allSearch(toFind);
    } else {
      this.indexSearch(toFind);
    }
  }

  /**
   * A getter method for the found field of MySearcher class.
   *
   * @return the list of matches with the search word, if any were found
   */
  public ArrayList<List<String>> getFound() {
    return new ArrayList<>(this.found);
  }
}
