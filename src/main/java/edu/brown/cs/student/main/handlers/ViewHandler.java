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
 * The ViewHandler class is responsible for handling GET requests for viewing the contents of a loaded
 * CSV. It takes in a Dataset and serializes the data into a JSON response or returns an error response
 * if no files are loaded.
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
   * Method that handles an HTTP request to retrieve and view the dataset's contents.
   * Constructs a success response with the contents or an error response in JSON format.
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

//    System.out.println(3);
//    Type type = Types.newParameterizedType(List.class, List.class, String.class);
//    Type type1 = Types.newParameterizedType(List.class, String.class);
//    JsonAdapter<List<String>> CSVDataAdapter1 = moshi.adapter(type1);
    //JsonAdapter<Dataset> CSVDataAdapter2 = moshi.adapter(Dataset.class);


//    System.out.println(4);
    Map<String, Object> responseMap = new HashMap<>();
//    System.out.println(5);
    try {
      List<List<String>> currentData = this.data.getDataset();
      if (currentData.isEmpty()) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "No files are loaded");
//        String json = adapter.toJson(responseMap);
//        System.out.println(json);
        return adapter.toJson(responseMap);
      }
//      List<String> row = new ArrayList<>();
//      for (List<String> rows : currentData) {
//        row = rows;
//        break;
//      }
//      String ex = "";
//      for (String ele : row){
//        ex = ele;
//        break;
//      }
//      responseMap.put("2nd example", ex);
//      responseMap.put("example", CSVDataAdapter1.toJson(row));
      responseMap.put("type", "success");
      responseMap.put("view data", currentData);
      String json = adapter.toJson(responseMap);
      System.out.println(json);
      return adapter.toJson(responseMap);
    } catch (Exception e) {
      responseMap.put("type", "error");
      responseMap.put("error_type", e);
      return adapter.toJson(responseMap);
//      System.out.println(e);
//      return "Booboo";
    }
  }

//  public record ViewSuccessResponse(String response_type, List<ArrayList<String>> contents) {
//
//    public ViewSuccessResponse(List<ArrayList<String>> contents) {
//      this("Success viewing loaded file!", contents);
//    }
//
//    /**
//     * @return this response, serialized as Json
//     */
//    // LOOK at soupAPI
//    String serialize() {
//      try {
//        // Just like in SoupAPIUtilities.
//        //   (How could we rearrange these similar methods better?)
//        Moshi moshi = new Moshi.Builder()
//            .add(
//                // Expect something that's a Dataset(List)...
//                PolymorphicJsonAdapterFactory.of(List.class, "outer")
//                    // ...with its inside being an arraylist?
//                    .withSubtype(ArrayList.class, "inner")
//            )
//            .build();
//        Type datasetType = Types.newParameterizedType(List.class, ArrayList.class);
//        JsonAdapter<List<ArrayList<String>>> adapter =moshi.adapter(datasetType);
////        JsonAdapter<ViewSuccessResponse> adapter = moshi.adapter(ViewSuccessResponse.class);
//        return adapter.toJson(contents);
//      } catch (Exception e) {
//        // For debugging purposes, show in the console _why_ this fails
//        // Otherwise we'll just get an error 500 from the API in integration
//        // testing.
//        e.printStackTrace();
//        throw e;
//      }
//    }
//  }

}
