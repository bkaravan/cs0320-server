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

public class ViewHandler implements Route {

  private final Dataset data;

  public ViewHandler(Dataset loaded) {
    this.data = loaded;
  }

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
      responseMap.put("view data", currentData);
      String json = adapter.toJson(responseMap);
      System.out.println(json);
      return adapter.toJson(responseMap);
    } catch (Exception e) {
      responseMap.put("type", "error");
      responseMap.put("error_type", e);
      return adapter.toJson(responseMap);
    }
  }

}
