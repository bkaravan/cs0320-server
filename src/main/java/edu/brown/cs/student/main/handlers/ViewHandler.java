package edu.brown.cs.student.main.handlers;

import edu.brown.cs.student.main.parser.MyParser;
import edu.brown.cs.student.main.server.Dataset;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.View;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewHandler implements Route {

  Dataset<ArrayList<String>> data;

  public ViewHandler(Dataset<ArrayList<String>> loaded) {
    this.data = loaded;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      List<ArrayList<String>> currentData = this.data.getDataset();
      if (currentData.isEmpty()) {
        return "No files loaded";
      }

      return "Success " + currentData;
    } catch (Exception e) {
      return "BooBoo";
    }
  }

}
