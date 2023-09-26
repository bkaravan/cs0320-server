package edu.brown.cs.student.main.server;

import spark.Request;
import spark.Response;
import spark.Route;

public class SearchHandler implements Route {

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // we either do a success response or a fail response

    // prompt user with questions - need word, is there header, (some sort of specifier?)
    // try calling mySearcher,
    return null;
  }
}
