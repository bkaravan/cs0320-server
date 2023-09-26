package edu.brown.cs.student.main;

import edu.brown.cs.student.main.parser.MyParser;
import edu.brown.cs.student.main.rowhandler.FactoryFailureException;
import edu.brown.cs.student.main.rowhandler.RowHandler;
import edu.brown.cs.student.main.searcher.MySearcher;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/** The Main class of our project. This is where execution begins. */
public final class Main {

  private String[] args;

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) throws IOException, FactoryFailureException {
    new Main(args).run();
  }

  /**
   * These specs are repeated in the README and are vital for program to run smoothly: args[0] -
   * string filepath args[1] - string searchWord args[2] - boolean for the header ("True"/"False")
   * args[3] - string for narrowing the search, that specifies either the name search or index
   * search example: "Ind: 0"; "Nam: Position".
   *
   * @param args the input parameters into the main function
   */
  private Main(String[] args) {
    this.args = args;
  }

  /** The method that runs Parser on the filepath and searcher on the dataset from the parser. */
  private void run() {
    try {
      String narrow = "";
      if (args.length < 3) {
        System.err.println("Please provide all necessary arguments: filepath, search word, header");
        System.exit(0);
      } else if (args.length == 3) {
        narrow = "NULL";
      } else {
        narrow = args[3];
      }
      FileReader myReader = new FileReader(args[0]);
      RowHandler rowHandler = new RowHandler();
      MyParser parser = new MyParser(new BufferedReader(myReader), rowHandler);
      parser.toParse();
      for (Object row : parser) {
        System.out.println(row);
      }
      //      System.out.println(parser.getDataset());
      boolean header = args[2].toLowerCase().equals("true");
      MySearcher searcher = new MySearcher(parser.getDataset(), header, narrow);
      searcher.findRows(args[1]);
      System.out.println(searcher.getFound());
    } catch (FileNotFoundException e) {
      System.err.println("Please make sure the path to your file is correct");
    }
  }
}
