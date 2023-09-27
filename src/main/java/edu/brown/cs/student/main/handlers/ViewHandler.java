package edu.brown.cs.student.main.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory;
import edu.brown.cs.student.main.parser.MyParser;
import edu.brown.cs.student.main.server.Dataset;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.View;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewHandler implements Route {

  private final Dataset data;

  public ViewHandler(Dataset loaded) {
    this.data = loaded;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {

    Moshi moshi = new Moshi.Builder().build();
//    System.out.println(1);
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
//    System.out.println(2);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
//    System.out.println(3);
    JsonAdapter<Dataset> CSVDataAdapter = moshi.adapter(Dataset.class);
//    System.out.println(4);
    Map<String, Object> responseMap = new HashMap<>();
//    System.out.println(5);
    try {
      List<List<String>> currentData = this.data.getDataset();
      if (currentData.isEmpty()) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "No files are loaded");
        String json = adapter.toJson(responseMap);
        System.out.println(json);
        return adapter.toJson(responseMap);
      }
      responseMap.put("type", "success");
      responseMap.put("view data", CSVDataAdapter.toJson(this.data));
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
