package edu.brown.cs.student.main.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.parser.MyParser;
import edu.brown.cs.student.main.rowhandler.CreatorFromRow;
import edu.brown.cs.student.main.rowhandler.FactoryFailureException;
import edu.brown.cs.student.main.server.Dataset;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * The LoadHandler class deals with requests related to loading CSV files. It expects a "filepath"
 * query parameter specifying the path to the CSV file to be loaded, taking in a specified Dataset.
 */
public class LoadHandler implements Route {
  private final Dataset data;

  /**
   * Constructs a new LoadHandler instance with the specified Dataset.
   *
   * @param current The dataset to be used for viewing.
   */
  public LoadHandler(Dataset current) {
    this.data = current;
  }

  /**
   * Method that handles an HTTP request to load a dataset from a file. The MyParser class is used
   * to parse the CSV file, and the `CreatorFromRow` interface and custom `Creator` class are used
   * to specify how rows from the CSV file are transformed into lists of strings. Upon successful
   * loading, it updates the dataset in the `Dataset` object, and if an error occurs during loading,
   * it generates a JSON response indicating the failure.
   *
   * @param request the HTTP request containing the file path to load.
   * @param response the HTTP response to be populated with success or failure messages.
   * @return a success message if the file is loaded successfully; otherwise, a loading failure
   *     message in JSON format.
   * @throws Exception if an error occurs during file loading or response construction.
   */
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

  /** A record representing a loading failure response. It can be serialized to JSON format. */
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
