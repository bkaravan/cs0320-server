package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import edu.brown.cs.student.main.handlers.BroadbandHandler;
import edu.brown.cs.student.main.handlers.LoadHandler;
import edu.brown.cs.student.main.handlers.SearchHandler;
import edu.brown.cs.student.main.handlers.ViewHandler;
import spark.Spark;

/**
 * The Server class acts as the central component that listens for incoming HTTP requests,
 * routes them to the appropriate handler, and sends back the corresponding responses using
 * the SparkJava framework. Endpoints associated with each handler—`loadcsv`, `viewcsv`, `searchcsv`, and `broadband`—
 * are set up here.
 */
public class Server {

  /**
   * The main method initializes the Spark web server, configures routes, and starts it.
   *
   * @param args The command line arguments (not used in this implementation).
   */
  public static void main(String[] args) {
    int port = 3232;
    //String path = args[0];
    Spark.port(port);

    after((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Allow-Methods", "*");
    });

//    record Dataset(ArrayList<ArrayList<String>> data) {}
    Dataset current = new Dataset();

    Spark.get("loadcsv", new LoadHandler(current));
    Spark.get("viewcsv", new ViewHandler(current));
    Spark.get("searchcsv", new SearchHandler(current));
    Spark.get("broadband", new BroadbandHandler());
    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + port);
  }

}
