package edu.brown.cs.student.main.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.searcher.MySearcher;
import edu.brown.cs.student.main.server.Dataset;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchHandler implements Route {
  private final Dataset data;

  public SearchHandler(Dataset current) {
    this.data = current;

  }

  // narrow = "ind: 0" "nam: someName"

  @Override
  public Object handle(Request request, Response response) throws Exception {
    String search = request.queryParams("search");
    String narrow = request.queryParams("narrow");
    String headerS = request.queryParams("header");

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();
    try {
      List<List<String>> currentData = this.data.getDataset();
      if (currentData.isEmpty()) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "No files are loaded");
//        return "No files!";
        return adapter.toJson(responseMap);
      }

      boolean header = headerS.equalsIgnoreCase("true");

      MySearcher searcher = new MySearcher(currentData, header, narrow);
      searcher.findRows(search);
      List<List<String>> found = searcher.getFound();

      responseMap.put("type", "success");
      System.out.println(found);
      responseMap.put("view data", searcher.getFound());
//      return "Somedata!";
      return adapter.toJson(responseMap);
    } catch (Exception e) {
      System.out.println(e);
      responseMap.put("type", "error");
      responseMap.put("error_type", e);
      return adapter.toJson(responseMap);
//      System.out.println(e);
//      return "Booboo";
    }

  }

}
