package edu.brown.cs.student.main.handlers;

import static spark.Spark.connect;

import java.io.BufferedReader;
import java.io.IOException;
import okio.Buffer;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import spark.Request;
import spark.Response;
import spark.Route;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.net.URLConnection;
import java.util.*;

public class BroadbandHandler implements Route {

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // parse request parameters
    String stateName = request.queryParams("state");
    String countyName = request.queryParams("county");
    // retrieve the numerical code for state and county
    String stateCode = getStateCode(stateName);
    String countyCode = getCountyCode(stateCode, countyName);

    // request for the state and county
    try {
      String apiUrl = "https://api.census.gov/data/2021/acs/acs1/subject/variables";
      String apiKey = "api_key";

      String apiUrlWithParams = apiUrl + "?get=NAME,S2802_C03_022E&for=county:" + countyCode +
          "*&in=state:" + stateCode + "&key=" + apiKey;

      URL url = new URL(apiUrlWithParams);

      // open connection to API
      HttpURLConnection requestURL = (HttpURLConnection) url.openConnection();

      int responseCode = requestURL.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {

        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<GridResponse> adapter = moshi.adapter(GridResponse.class).nonNull();
        // NOTE: important! pattern for handling the input stream
        GridResponse body = adapter.fromJson(new Buffer().readFrom(requestURL.getInputStream()));
        requestURL.disconnect();
        if (body == null || body.properties() == null || body.properties().gridId() == null) {
          throw new DatasourceException("Malformed response from Census API");
        }

        // return the extracted census data
        return body;
      } else {
        // handle failed API request
        throw new DatasourceException("API request failed with response code: " + responseCode);
      }

    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }

//    try {
//      URL requestURL = new URL("https", "api.weather.gov", "/points/"+lat+","+lon);
//      HttpURLConnection clientConnection = connect(requestURL);
//      Moshi moshi = new Moshi.Builder().build();
//      JsonAdapter<GridResponse> adapter = moshi.adapter(GridResponse.class).nonNull();
//      // NOTE: important! pattern for handling the input stream
//      GridResponse body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
//      clientConnection.disconnect();
//      if(body == null || body.properties() == null || body.properties().gridId() == null)
//        throw new DatasourceException("Malformed response from NWS");
//      return body;
//    } catch(IOException e) {
//      throw new DatasourceException(e.getMessage());
//    }

  }

  private String getStateCode(String stateName) throws IOException {
    // make an API request to get the state code based on the state name provided
    String apiUrl = "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*";
    HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();

    try {
      connection.setRequestMethod("GET");
      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
       // parse through to get the state code
      }
    } finally {
      connection.disconnect();
    }
    // Return null if state name not found?
    return null;
  }

  private String getCountyCode(String stateCode, String countyName) throws IOException {
    // make an API request to get the state code based on the county name and state code provided
    String apiUrl = "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode;
    HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();

    try {
      connection.setRequestMethod("GET");
      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // get the county code
      }
    } finally {
      connection.disconnect();
    }

    // Return null if county name not found?
    return null;
  }

  public record GridResponse(String id, GridResponseProperties properties) { }
  // Note: case matters! "gridID" will get populated with null, because "gridID" != "gridId"
  public record GridResponseProperties(String gridId, String gridX, String gridY, String timeZone, String radarStation) {}

}
