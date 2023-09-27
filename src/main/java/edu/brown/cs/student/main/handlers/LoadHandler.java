package edu.brown.cs.student.main.handlers;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.parser.MyParser;
import edu.brown.cs.student.main.rowhandler.CreatorFromRow;
import edu.brown.cs.student.main.rowhandler.FactoryFailureException;
import edu.brown.cs.student.main.rowhandler.RowHandler;
import edu.brown.cs.student.main.server.Dataset;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadHandler implements Route {
  private final Dataset data;
  // create a parser field? feed in the parser here?

  public LoadHandler(Dataset current) {
    this.data = current;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // we either do a success response or a fail response
    String path = request.queryParams("filepath");
    try {
      FileReader freader = new FileReader(path);
//      RowHandler creator = new RowHandler();
      class Creator implements CreatorFromRow<List<String>> {
        @Override
        public List<String> create(List<String> row) throws FactoryFailureException {
          return row;
        }
      }

      MyParser<List<String>> parser = new MyParser<>(freader, new Creator());
      parser.toParse();
      this.data.setDataset(parser.getDataset());
      return "File " + path + " loaded successfully!";
    } catch (IOException e) {
      return new LoadingFailureResponse("Error loading file: " + path).serialize();
    }
  }

  public record LoadingFailureResponse(String response_type) {
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadingFailureResponse.class).toJson(this);
    }
  }

}
