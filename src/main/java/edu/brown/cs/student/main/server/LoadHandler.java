package edu.brown.cs.student.main.server;

import java.io.FileReader;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadHandler implements Route {

  private String path;
  // create a parser field? feed in the parser here?

  public LoadHandler() {
    // this.path = path;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // we either do a success response or a fail response
    this.path = request.queryParams("filepath");
    try {
      FileReader freader = new FileReader(this.path);
      // a good response can be something like yay we loaded in the CSV!
    } catch (Exception e) {
      // return a bad response to say that we couldn't instantiate CSV
      // return badanswer;
    }

    return null;
  }
}
