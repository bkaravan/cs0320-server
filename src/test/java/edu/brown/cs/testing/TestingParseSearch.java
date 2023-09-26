package edu.brown.cs.testing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.assertEquals;

import edu.brown.cs.student.main.parser.MyParser;
import edu.brown.cs.student.main.parser.ParsedRow;
import edu.brown.cs.student.main.rowhandler.FactoryFailureException;
import edu.brown.cs.student.main.rowhandler.RowHandler;
import edu.brown.cs.student.main.rowhandler.SecondRowHandler;
import edu.brown.cs.student.main.searcher.MySearcher;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testng.AssertJUnit;

/** Testing suit for this project. */
public class TestingParseSearch {

  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  private ArrayList<ArrayList<String>> testSet;
  private RowHandler creator = new RowHandler();
  private MySearcher searcher;
  private MyParser parser;

  /** A simple test that creates a dataset out of a stringReader. */
  @Test
  public void parseStringReader() {
    String example =
        """
             value1, value2, value3
             value4, value5, value6,
             value7, value8, value9
            """;
    StringReader read = new StringReader(example);
    this.parser = new MyParser(read, this.creator);
    this.parser.toParse();
    this.testSet = parser.getDataset();
    assertEquals(3, this.testSet.size());
  }

  /** Test that checks that contents of the dataset after parser is done running are correct. */
  @Test
  public void parseCheckDataset() {
    String example =
        """
             value1, value2, value3
             value4, value5, value6,
             value7, value8, value9
            """;
    StringReader read = new StringReader(example);
    this.parser = new MyParser(read, this.creator);
    this.parser.toParse();
    this.testSet = parser.getDataset();
    ArrayList<String> row1 = new ArrayList<>(Arrays.asList("value3", "value5", "value6"));
    ArrayList<String> row2 = new ArrayList<>(Arrays.asList("value4", "value5", "value6"));
    assertTrue(this.testSet.contains(row2));
    assertFalse(this.testSet.contains(row1));
  }

  /**
   * Test that parses a given file from a filepath and checks that it contains values from the csv
   * file. This file also contains empty entries, so it checks my FactoryFailureException - when the
   * program encounters a row with an empty entry, it will not parse it and print a message, but the
   * program won't terminate
   *
   * @throws FileNotFoundException
   */
  @Test
  public void parseFileReaderWithFactoryFailure() throws FileNotFoundException {
    String filepath = "data/stars/ten-star.csv";
    this.parser = new MyParser(new FileReader(filepath), this.creator);
    this.parser.toParse();
    this.testSet = parser.getDataset();
    assertEquals(7, this.testSet.size());
    ArrayList<String> row1 =
        new ArrayList<>(Arrays.asList("3759", "96 G. Psc", "7.26388", "1.55643", "0.68697"));
    ArrayList<String> row2 =
        new ArrayList<>(Arrays.asList("96 G. Psc", "7.26388", "1.55643", "0.68697"));
    assertTrue(this.testSet.contains(row1));
    assertFalse(this.testSet.contains(row2));
  }

  /**
   * A test that checks if the parser can handle the files of bigger size.
   *
   * @throws FileNotFoundException
   */
  @Test
  public void parseBigFile() throws FileNotFoundException {
    String filepath = "data/census/income_by_race_edited.csv";
    this.parser = new MyParser(new FileReader(filepath), this.creator);
    this.parser.toParse();
    this.testSet = parser.getDataset();
    assertEquals(324, this.testSet.size());
  }

  /**
   * A test case to check the parser with another implementation of CreaterFromRow interface.
   *
   * @throws FactoryFailureException
   * @throws FileNotFoundException
   */
  @Test
  public void parseDifferentGeneric() throws FactoryFailureException, FileNotFoundException {
    String filepath = "data/csvtest/noHeaderTest.csv";
    this.parser = new MyParser(new FileReader(filepath), new SecondRowHandler());
    this.parser.toParse();
    ArrayList<ParsedRow> testSet1 = parser.getDataset();
    assertEquals(3, testSet1.size());
    assertEquals("1, Joe, MetCalf, 330", testSet1.get(1).toString());
  }

  /**
   * An assertThrow test to make sure that an error will be thrown when the user might use an
   * incorrect filepath.
   */
  @Test
  public void fileNotFoundTest() {
    String filepath = "Not a File!";
    assertThrows(
        FileNotFoundException.class, () -> new MyParser(new FileReader(filepath), this.creator));
  }

  /**
   * Search test that correctly finds a value from the string reader without header or index specs.
   */
  @Test
  public void searchFoundNoNarrowNoHeader() {
    String example =
        """
             value1, value2, value3
             value4, value5, value6,
             value7, value8, value9
            """;
    StringReader read = new StringReader(example);
    this.parser = new MyParser(read, this.creator);
    this.parser.toParse();
    this.searcher = new MySearcher(parser.getDataset(), false, "NULL");
    this.searcher.findRows("value5");
    ArrayList<String> compare = new ArrayList<>(List.of("value4", "value5", "value6"));
    ArrayList<String> res = new ArrayList<>(this.searcher.getFound().get(0));
    assertEquals(res, compare);
    assertEquals(1, this.searcher.getFound().size());
  }

  /** Searcher test that correctly doesn't find the search word that is not present. */
  @Test
  public void searchNotFoundNoNarrowNoHeader() {
    String example =
        """
             value1, value2, value3
             value4, value5, value6,
             value7, value8, value9
            """;
    StringReader read = new StringReader(example);
    this.parser = new MyParser(read, this.creator);
    this.parser.toParse();
    this.searcher = new MySearcher(parser.getDataset(), false, "NULL");
    this.searcher.findRows("supervalue");
    assertEquals(0, this.searcher.getFound().size());
  }

  /** A searcher test to check that we are allowed to check by indices without a header. */
  @Test
  public void searchFoundNarrowNoHeader() {
    String example =
        """
             value1, value2, value3
             value4, value5, value6,
             value7, value8, value9
            """;
    StringReader read = new StringReader(example);
    this.parser = new MyParser(read, this.creator);
    this.parser.toParse();
    this.searcher = new MySearcher(parser.getDataset(), false, "Ind: 2");
    this.searcher.findRows("value9");
    ArrayList<String> compare = new ArrayList<>(List.of("value7", "value8", "value9"));
    assertEquals(1, this.searcher.getFound().size());
    assertEquals(compare, this.searcher.getFound().get(0));
  }

  /**
   * Searcher test that correctly finds the search word when there is a header and no index specs.
   *
   * @throws FileNotFoundException
   */
  @Test
  public void searchFoundNoNarrowHeader() throws FileNotFoundException {
    String filepath = "data/csvtest/test.csv";
    this.parser = new MyParser(new FileReader(filepath), this.creator);
    this.parser.toParse();
    this.searcher = new MySearcher(parser.getDataset(), true, "NULL");
    this.searcher.findRows("right");
    assertEquals(this.searcher.getFound().size(), 2);
  }

  /**
   * Test that checks that if the search word repeats in the header column name, it only gets found
   * in the "body" of the dataset.
   *
   * @throws FileNotFoundException
   */
  @Test
  public void searchHeaderDuplicate() throws FileNotFoundException {
    String filepath = "data/csvtest/duplicate.csv";
    this.parser = new MyParser(new FileReader(filepath), this.creator);
    this.parser.toParse();
    this.searcher = new MySearcher(parser.getDataset(), true, "NULL");
    this.searcher.findRows("Country");
    assertEquals(1, this.searcher.getFound().size());
    assertTrue(this.searcher.getFound().contains(List.of("Kozelets", "Chernihiv", "Country")));
  }

  /**
   * Test that correctly finds a search word given a header and a name index specifies.
   *
   * @throws FileNotFoundException
   */
  @Test
  public void searchFoundNarrowHeader() throws FileNotFoundException {
    String filepath = "data/csvtest/test.csv";
    this.parser = new MyParser(new FileReader(filepath), this.creator);
    this.parser.toParse();
    this.searcher = new MySearcher(parser.getDataset(), true, "NAM: class");
    this.searcher.findRows("second");
    ArrayList<String> compare = new ArrayList<>(List.of("bohdan", "second", "left"));
    ArrayList<String> compare0 = new ArrayList<>(List.of("jake", "second", "right"));
    assertEquals(this.searcher.getFound().size(), 2);
    assertEquals(this.searcher.getFound().get(1), compare);
    assertEquals(this.searcher.getFound().get(0), compare0);
  }

  /**
   * Test that checks that if we specify the name of the column, and the search word is in the
   * dataset but is not in that column, it DOES NOT get found.
   *
   * @throws FileNotFoundException
   */
  @Test
  public void searchNotFoundWrongNarrow() throws FileNotFoundException {
    String filepath = "data/csvtest/test.csv";
    this.parser = new MyParser(new FileReader(filepath), this.creator);
    this.parser.toParse();
    this.searcher = new MySearcher(parser.getDataset(), true, "nam: class");
    this.searcher.findRows("bohdan");
    assertEquals(this.searcher.getFound().size(), 0);
  }

  /**
   * Test to see that searcher can look up by index column when the header row is present.
   *
   * @throws FileNotFoundException
   */
  @Test
  public void searchFoundIndexWithHeader() throws FileNotFoundException {
    String filepath = "data/stars/stardata.csv";
    this.parser = new MyParser(new FileReader(filepath), this.creator);
    this.parser.toParse();
    this.searcher = new MySearcher(parser.getDataset(), true, "ind: 1");
    this.searcher.findRows("Cael");
    ArrayList<String> compare =
        new ArrayList<>(List.of("11", "Cael", "159.15237", "0.1036", "170.31215"));
    assertEquals(this.searcher.getFound().size(), 1);
    assertEquals(this.searcher.getFound().get(0), compare);
  }

  /**
   * Test to see that if there was a mistake in providing the name of the column, the searcher
   * defaults to looking through the whole dataset and can still find the row.
   */
  @Test
  public void searchWithMistake() throws FileNotFoundException {
    String filepath = "data/csvtest/test.csv";
    this.parser = new MyParser(new FileReader(filepath), this.creator);
    this.parser.toParse();
    this.searcher = new MySearcher(parser.getDataset(), true, "nam: not there!");
    this.searcher.findRows("right");
    assertEquals(2, this.searcher.getFound().size());
  }

  /**
   * breaking regex test to see how it handles an example of a CSV row, which has quotation marks on
   * its ends.
   */
  @Test
  public void testQuotesWithCommasFullLine() {
    String line = "\"Hello, traveller, how, is, life\"";
    String[] result = regexSplitCSVRow.split(line);
    Assertions.assertEquals(1, result.length);
    AssertJUnit.assertFalse(result.length == 5);
    Assertions.assertEquals(
        "Hello, traveller, how, is, life", result[0].trim().replaceAll("\"", ""));
    // Looks like what happens is it can't separate the text within the quotes, so, for example,
    // if we will have a row that starts and ends with a quote, we won't be able to match any
    // words inside that row

    Assertions.assertEquals("Hello, traveller, how, is, life", postprocess(result[0]));
  }

  /** braking regex test to see how it handles the input quote that has another quote within it. */
  @Test
  public void testDoubleQuotesCommas() {
    String line = "\"I, hope, you, will, have, a, \"great, CS32\", experience\"";
    String[] result = regexSplitCSVRow.split(line);
    Assertions.assertEquals(2, result.length);
    // Another point of interest is this example, here, we imitate that our input row is a quote,
    // and inside it there is another quote, the split happens with the comma that's in the inside
    // quote; If you take a look at prints and the last two tests, it will actually leave one of
    // the quotation marks next to words of that string, which is definitely not ideal
    Assertions.assertEquals(
        "I, hope, you, will, have, a, great", result[0].trim().replaceAll("\"", ""));
    Assertions.assertEquals("CS32, experience", result[1].trim().replaceAll("\"", ""));

    // the first input is what we might expect, but it's not!
    assertNotEquals("I, hope, you, will, have, a, great", postprocess(result[0]));
    assertNotEquals("CS32, experience", postprocess(result[1]));
  }

  /**
   * Elimiate a single instance of leading or trailing double-quote, and replace pairs of double
   * quotes with singles.
   *
   * @param arg the string to process
   * @return the postprocessed string
   */
  public static String postprocess(String arg) {
    return arg
        // Remove extra spaces at beginning and end of the line
        .trim()
        // Remove a beginning quote, if present
        .replaceAll("^\"", "")
        // Remove an ending quote, if present
        .replaceAll("\"$", "")
        // Replace double-double-quotes with double-quotes
        .replaceAll("\"\"", "\"");
  }
}
