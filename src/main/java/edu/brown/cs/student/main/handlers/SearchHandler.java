package edu.brown.cs.student.main.handlers;

import edu.brown.cs.student.main.server.Dataset;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchHandler implements Route {
  private Dataset data;

  public SearchHandler(Dataset current) {
    this.data = current;

  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // we either do a success response or a fail response
    return null;
  }

}
