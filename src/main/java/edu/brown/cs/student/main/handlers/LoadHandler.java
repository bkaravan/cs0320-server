package edu.brown.cs.student.main.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.parser.MyParser;
import edu.brown.cs.student.main.rowhandler.CreatorFromRow;
import edu.brown.cs.student.main.rowhandler.FactoryFailureException;
import edu.brown.cs.student.main.rowhandler.RowHandler;
import edu.brown.cs.student.main.server.Dataset;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();
    String path = request.queryParams("filepath");
    if (path == null) {
      responseMap.put("type", "error");
      responseMap.put("error_type", "missing_argument");
      responseMap.put("missing_argument", "filepath");
      return adapter.toJson(responseMap);
    }
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
      responseMap.put("result", "success");
      responseMap.put("loaded", path);
      return adapter.toJson(responseMap);
    } catch (IOException e) {
      return new LoadingFailureResponse("error_datasource: " + path).serialize();
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
