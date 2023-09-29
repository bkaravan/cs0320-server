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
    //setup moshi
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();
    try {
      //check if we loaded anything
      List<List<String>> currentData = this.data.getDataset();
      if (currentData.isEmpty()) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "No files are loaded");
        return adapter.toJson(responseMap);
      }

      String search = request.queryParams("search");
      // check search parameter - required
      if (search == null) {
        responseMap.put("type:", "error");
        responseMap.put("error_type:", "missing_parameter");
        responseMap.put("error_arg: ", "search");
        return adapter.toJson(responseMap);
      }
      String headerS = request.queryParams("header");
      // check header parameter - required
      if (headerS == null) {
        responseMap.put("type:", "error");
        responseMap.put("error_type:" , "missing_parameter");
        responseMap.put("error_arg: ", "header");
        return adapter.toJson(responseMap);
      }
      String narrow = request.queryParams("narrow");
      //check narrow parameter = not required
      if (narrow == null) {narrow = "NULL";}
      boolean header = headerS.equalsIgnoreCase("true");

      // initialize the searcher and look for the words
      MySearcher searcher = new MySearcher(currentData, header, narrow);
      searcher.findRows(search);
      List<List<String>> found = searcher.getFound();
      // check the found - throw an exception in searcher?
      if (found.isEmpty()) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "no match found");
        responseMap.put("search word", search);
        return adapter.toJson(responseMap);
      }

      responseMap.put("result", "success");
      responseMap.put("view data", found);
      return adapter.toJson(responseMap);
    } catch (Exception e) {
      System.out.println(e);
      responseMap.put("type", "error");
      responseMap.put("error_type", e);
      return adapter.toJson(responseMap);
    }
  }
}
