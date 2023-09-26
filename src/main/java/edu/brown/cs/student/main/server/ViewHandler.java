package edu.brown.cs.student.main.server;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.server.LoadHandler.LoadingFailureResponse;
import java.util.ArrayList;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewHandler implements Route {

  public ArrayList<ArrayList<String>> loaded = new ArrayList<>();

  public ViewHandler() {
    //this.loaded = data;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // we either do a success response or a fail response
    if (this.loaded.isEmpty()) {
      return new ViewFailureResponse().serialize();
      //return new ViewFailureResponse().serialize();
    }
    return "S";
  }

  public record ViewFailureResponse(String response_type) {
    public ViewFailureResponse() { this("Error: No files are loaded"); }

    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(ViewHandler.ViewFailureResponse.class).toJson(this);
    }
  }
}
