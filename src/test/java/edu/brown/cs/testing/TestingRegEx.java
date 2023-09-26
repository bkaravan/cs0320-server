package edu.brown.cs.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Example that shows some regular-expression processing for CSV rows. Note: this regexp may not be
 * perfect, but it *should* suffice for this sprint.
 *
 * <p>Recall that Java "escapes" double-quote characters via backslash. So the double-quote
 * character, within a string, should be written as: \"
 */
public class TestingRegEx {

  static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  /** Basic comma-separation; we could have done this with String.split(","). */
  @Test
  public void testBasic() {
    String line = "Hello, World, 123, ABC";
    String[] result = regexSplitCSVRow.split(line);

    assertEquals(4, result.length);
    assertEquals("Hello", result[0].trim());
    assertEquals("World", result[1].trim());
    assertEquals("123", result[2].trim());
    assertEquals("ABC", result[3].trim());
  }

  /**
   * But what if a value needs to contain a comma literal? In this case, we need to use a heavier
   * hammer. For this example, we'll use a regular expression that is defined above.
   */
  @Test
  public void testQuotes() {
    String line = "\"Providence, RI\", 123.456, \"Telson, Nim\"";
    String[] result = regexSplitCSVRow.split(line);

    assertEquals(3, result.length);
    assertEquals("Providence, RI", result[0].trim().replaceAll("\"", ""));
    assertEquals("123.456", result[1].trim().replaceAll("\"", ""));
    assertEquals("Telson, Nim", result[2].trim().replaceAll("\"", ""));
  }

  /**
   * It gets worse. What if the value needs to, itself, contain double quotes? The convention here
   * is to treat a pair of double-quote characters as a single double-quote literal.
   */
  @Test
  public void testQuotesWithEscaping() {
    String line =
        "\"Regular expressions are \"\"fun\"\"\", \"However, sometimes they are \"\"useful\"\"\"";
    String[] result = regexSplitCSVRow.split(line);

    assertEquals(2, result.length);
    assertEquals("Regular expressions are fun", result[0].trim().replaceAll("\"", ""));
    assertEquals("However, sometimes they are useful", result[1].trim().replaceAll("\"", ""));

    // Wait a minute; that's not a careful test. Don't we want to confirm we still have the
    // double-quote literals?
    // The challenge is that the regex above KEEPS all double quote characters. So let's be careful.
    // We'll make a
    // helper function to do the post-processing for us.
    System.out.println(postprocess(result[0]));
    assertEquals("Regular expressions are \"fun\"", postprocess(result[0]));
    assertEquals("However, sometimes they are \"useful\"", postprocess(result[1]));
  }

  /** regex test to see how it handles */
  @Test
  public void testQuotesWithCommasSingleInstance() {
    String line = "Hello, \"traveller\", how, is, life";
    String[] result = regexSplitCSVRow.split(line);
    System.out.println(Arrays.asList(result));
    assertEquals(5, result.length);
    assertEquals("Hello", result[0].trim().replaceAll("\"", ""));
    assertEquals("traveller", result[1].trim().replaceAll("\"", ""));

    System.out.println(postprocess(result[0]));
    assertEquals("Hello", postprocess(result[0]));
    assertEquals("traveller", postprocess(result[1]));
  }

  /**
   * breaking regex test to see how it handles an example of a CSV row, which has quotation marks on
   * its ends.
   */
  @Test
  public void testQuotesWithCommasFullLine() {
    String line = "\"Hello, traveller, how, is, life\"";
    String[] result = regexSplitCSVRow.split(line);
    System.out.println(Arrays.asList(result));
    assertEquals(1, result.length);
    assertFalse(result.length == 5);
    assertEquals("Hello, traveller, how, is, life", result[0].trim().replaceAll("\"", ""));
    // Looks like what happens is it can't separate the text within the quotes, so, for example,
    // if we will have a row that starts and ends with a quote, we won't be able to match any
    // words inside that row

    System.out.println(postprocess(result[0]));
    assertEquals("Hello, traveller, how, is, life", postprocess(result[0]));
  }

  /** braking regex test to see how it handles the input quote that has another quote within it. */
  @Test
  public void testDoubleQuotesCommas() {
    String line = "\"I, hope, you, will, have, a, \"great, CS32\", experience\"";
    String[] result = regexSplitCSVRow.split(line);
    System.out.println(Arrays.asList(result));
    assertEquals(2, result.length);
    // Another point of interest is this example, here, we imitate that our input row is a quote,
    // and inside it there is another quote, the split happens with the comma that's in the inside
    // quote; If you take a look at prints and the last two tests, it will actually leave one of
    // the quotation marks next to words of that string, which is definitely not ideal
    assertEquals("I, hope, you, will, have, a, great", result[0].trim().replaceAll("\"", ""));
    assertEquals("CS32, experience", result[1].trim().replaceAll("\"", ""));

    System.out.println(postprocess(result[0]));
    System.out.println(postprocess(result[1]));
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
