package edu.brown.cs.student.main.parser;

import edu.brown.cs.student.main.rowhandler.CreatorFromRow;
import edu.brown.cs.student.main.rowhandler.FactoryFailureException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * MyParser is a parser class that is responsible for going through the CSV file and creating a
 * certain database out of it.
 */
public class MyParser<T> implements Iterable<T> {

  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
  private final ArrayList<T> dataset;
  private final CreatorFromRow<T> creator;
  private final BufferedReader buffreader;
  private String line;
  private int index;

  /**
   * constructor for MyParser class. It takes in a Reader object and creates a buffered reader out
   * of it, and an instance of a class that implements the CreatorFromRow interface that uses a
   * generic type T. This type is used to create Rows and store them in the dataset, which is an
   * ArrayList of objects of type T.
   *
   * @param obj a reader object
   * @param creator an object that implements the creatorFromRow interface that is responsible for
   *     creating rows
   */
  public MyParser(Reader obj, CreatorFromRow<T> creator) {
    this.buffreader = new BufferedReader(obj);
    this.creator = creator;
    this.dataset = new ArrayList<>();
    this.line = "";
    this.index = 0;
  }

  /**
   * Method that uses the reader field to go through the file and parse each row using create, and
   * creates a dataset of every row. If it encounters a FactureFailure exception, it will print a
   * message that the row is not passed into the dataset, but will keep going through the file
   */
  public void toParse() {
    boolean keepGoing = true;
    while (keepGoing) {
      try {
        this.line = this.buffreader.readLine();
        while (this.line != null) {
          // this.dataset.add(this.creator.create(Arrays.asList(this.line.split(","))));
          this.dataset.add(this.creator.create(Arrays.asList(regexSplitCSVRow.split(this.line))));
          this.line = this.buffreader.readLine();
          this.index++;
        }
        keepGoing = false;
        this.buffreader.close();
      } catch (IOException e) {
        System.out.println("Error " + e);
      } catch (FactoryFailureException e) {
        System.out.println("Row with index " + this.index + " was not processed. Error: " + e);
        this.index++;
        if (this.line == null) {
          keepGoing = false;
        }
      }
    }
  }

  /**
   * A getter method to pass the parsed information into the searcher.
   *
   * @return the dataset, which is an ArrayList of T objects after parsing the file
   */
  public ArrayList<T> getDataset() {
    return new ArrayList<>(this.dataset);
  }

  public Iterator<T> iterator() {
    return new ParseIterator<T>(this.dataset);
  }
}
