package edu.brown.cs.student.main.server;

import static spark.Spark.after;

import java.util.ArrayList;
import java.util.List;
import spark.Spark;

public class Server {
  public static void main(String[] args) {
    int port = 3232;
    // String path = args[0];
    ArrayList<ArrayList<String>> parsedData = new ArrayList<>();
    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    LoadHandler load = new LoadHandler();
    Spark.get("loadcsv", new LoadHandler());
    //parsedData = load.parseddata;
    Spark.get("viewcsv", new ViewHandler()); // pass in the whole parser?
    Spark.get("searchcsv", new SearchHandler()); // pass in searcher?
    Spark.get("broadband", new BroadbandHandler()); // something with internet database
    Spark.init();
    Spark.awaitInitialization();

    System.out.println("Server started at http://localhost:" + port);
  }
}
