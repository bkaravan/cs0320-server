package edu.brown.cs.student.main.server;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.parser.MyParser;
import edu.brown.cs.student.main.rowhandler.CreatorFromRow;
import edu.brown.cs.student.main.rowhandler.RowHandler;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadHandler implements Route {

  private String path;
  public ArrayList parseddata;
  // create a parser field? feed in the parser here?

  public LoadHandler() {}

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // we either do a success response or a fail response
    this.path = request.queryParams("filepath");
    try {
      FileReader freader = new FileReader(this.path);
      RowHandler creator = new RowHandler();

      MyParser parser = new MyParser(freader, creator);
      parser.toParse();
      this.parseddata = parser.getDataset();

      return "File " + this.path + " loaded successfully!";
    } catch (IOException e) {
      return new LoadingFailureResponse().serialize();
    }
  }

  public record LoadingFailureResponse(String response_type) {
    public LoadingFailureResponse() { this("Error opening your file"); }

    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadingFailureResponse.class).toJson(this);
    }
  }

}
