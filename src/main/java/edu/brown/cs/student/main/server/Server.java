package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import edu.brown.cs.student.main.handlers.BroadbandHandler;
import edu.brown.cs.student.main.handlers.LoadHandler;
import edu.brown.cs.student.main.handlers.SearchHandler;
import edu.brown.cs.student.main.handlers.ViewHandler;
import spark.Spark;

public class Server<T> {
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
    Spark.get("viewcsv", new ViewHandler(current)); //pass in the whole parser?
    Spark.get("searchcsv", new SearchHandler(current)); //pass in searcher?
    Spark.get("broadband", new BroadbandHandler()); // something with internet database
    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + port);
  }

}
