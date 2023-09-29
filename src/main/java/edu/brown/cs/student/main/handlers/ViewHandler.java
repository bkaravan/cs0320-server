package edu.brown.cs.student.main.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.Dataset;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * The ViewHandler class is responsible for handling GET requests for viewing the contents of a
 * loaded CSV. It takes in a Dataset and serializes the data into a JSON response or returns an
 * error response if no files are loaded.
 */
public class ViewHandler implements Route {

  private final Dataset data;

  /**
   * Constructs a new ViewHandler instance with the specified Dataset.
   *
   * @param loaded the Dataset to be used for viewing.
   */
  public ViewHandler(Dataset loaded) {
    this.data = loaded;
  }

  /**
   * Method that handles an HTTP request to retrieve and view the dataset's contents. Constructs a
   * success response with the contents or an error response in JSON format.
   *
   * @param request  the HTTP request.
   * @param response the HTTP response to be populated with dataset contents or error messages.
   * @return an HTTP response containing the dataset's contents or error messages in JSON format.
   * @throws Exception if an error occurs during dataset retrieval or response construction.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);

    Map<String, Object> responseMap = new HashMap<>();

    try {
      List<List<String>> currentData = this.data.getDataset();
      if (currentData.isEmpty()) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "No files are loaded");
        return adapter.toJson(responseMap);
      }
      responseMap.put("result", "success");
      responseMap.put("viewData", currentData);
      return adapter.toJson(responseMap);
    } catch (Exception e) {
      responseMap.put("type", "error");
      responseMap.put("error_type", e);
      return adapter.toJson(responseMap);
    }
  }
}
