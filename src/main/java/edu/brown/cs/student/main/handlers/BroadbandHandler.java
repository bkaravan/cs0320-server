package edu.brown.cs.student.main.handlers;

import static spark.Spark.connect;

import com.squareup.moshi.Types;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
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

//  public class CensusApiResponse {
//    private List<String> headers;
//    private List<StateInfo> states;
//
//    // Getters and setters for headers and states
//
//    public static class StateInfo {
//      private String name;
//      private String code;
//
//      // Getters and setters for name and code
//    }
//  }


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
      connection.connect();

      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<List<List<String>>> adapter = moshi.adapter(Types.newParameterizedType(List.class, List.class));

        // parse the json response into a List
        List<List<String>> jsonResponse = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));

        // iterate through the list to find the state code for the target state name
        for (List<String> row : jsonResponse) {
          if (row.size() >= 2) {
            String name = row.get(0);
            String code = row.get(1);

            if (stateName.equalsIgnoreCase(name)) {
              System.out.println(stateName + ": " + code);
              return code;
            }
          }
        }
      }
    } finally {
      connection.disconnect();
//    try {
//      connection.setRequestMethod("GET");
//      connection.connect();
//
//      //System.out.println("hi");
//
//      int responseCode = connection.getResponseCode();
//
//      // if connection success
//      if (responseCode == HttpURLConnection.HTTP_OK) {
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String inputLine;
//        StringBuilder jsonResponse = new StringBuilder();
//
//        while ((inputLine = in.readLine()) != null) {
//          jsonResponse.append(inputLine);
//        }
//        in.close();

        // The jsonResponse StringBuilder now contains the JSON response

        // Process the JSON data as needed



        // Sample JSON response (replace with your actual response)
//        String jsonResponse = "[[\"NAME\",\"state\"], [\"Alabama\",\"01\"], [\"Alaska\",\"02\"], ...]";
//
//        Moshi moshi = new Moshi.Builder().build();
//        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
//        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
//
//
//        String stateCode = null;

        // Iterate through the list and find the state code for the target state name
//        for (List<String> row : jsonResponse) {
//          if (row.size() >= 2) {
//            String name = row.get(0);
//            String code = row.get(1);
//
//            if (stateName.equals(name)) {
//              stateCode = code;
//              break; // Exit the loop when the state name is found
//            }
//          }
//        }


//        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

//        // Create a Moshi adapter
//        Moshi moshi = new Moshi.Builder().build();
//        JsonAdapter<CensusApiResponse> adapter = moshi.adapter(CensusApiResponse.class);
//
//        // Deserialize the JSON response into a CensusApiResponse object
//        CensusApiResponse response = adapter.fromJson(apiResponseJson);


//        String line;
//        while ((line = reader.readLine()) != null) {
//          // Assuming the response is in CSV format, parse it to extract state codes
//
//
//
//          System.out.println(line);
//        }
//      }
//    } finally {
//      connection.disconnect();
    }
    return null;
  }

  private String getCountyCode(String stateCode, String countyName) throws IOException {
    // api request to get county code based on county name and state code
    if (stateCode != null) {
      String apiUrl = "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode;

      HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();

      try {
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
          Moshi moshi = new Moshi.Builder().build();
          JsonAdapter<List<List<String>>> adapter = moshi.adapter(Types.newParameterizedType(List.class, List.class));

          // Parse the JSON response into a List
          List<List<String>> jsonResponse = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));

          // Iterate through the list to find the county code for the target county name
          for (List<String> row : jsonResponse) {
            if (row.size() >= 3) {
              String fullName = row.get(0);
//              System.out.println("llll" + fullName + "llll");
//              String sCode = row.get(1);
              String countyCode = row.get(2);
              // Split the full name to extract the county name
              String[] parts = fullName.split(",");
//              System.out.println(parts);
              if (parts.length >= 2) {
                String cName = parts[0].trim(); // Extracted county name
//                System.out.println(cName);

                // Check if the county name and state match the user's request
                if (countyName.equalsIgnoreCase(cName)) {
                  System.out.println(cName + ": " + countyCode);
                  return countyCode;
                }
              }
            }

          }
        }
      } finally {
        connection.disconnect();
      }
    }

    // Return null if county name not found
    return null;
  }

  public record GridResponse(String id, GridResponseProperties properties) { }
  // Note: case matters! "gridID" will get populated with null, because "gridID" != "gridId"
  public record GridResponseProperties(String gridId) {}

}
