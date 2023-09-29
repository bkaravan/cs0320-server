package edu.brown.cs.student.main.handlers;

import static spark.Spark.connect;

import com.squareup.moshi.Types;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
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
    // get date and time
    String timestamp = getDateTime();
    // request parameters
    String stateName = request.queryParams("state");
    String countyName = request.queryParams("county");
    // retrieve code for state and county
    String stateCode = getStateCode(stateName);
    String countyCode = getCountyCode(stateCode, countyName);

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter2 = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();

    System.out.println(timestamp);

    // request data for given state and county
    if (stateCode != null && countyCode != null) {
      System.out.println(1);

      try {
        String apiKey = "api_key";
        String apiUrl = "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_001E&for=county:" + countyCode + "&in=state:" + stateCode;
        System.out.println(apiUrl);
        System.out.println(2);

        URL url = new URL(apiUrl);

        HttpURLConnection requestURL = (HttpURLConnection) url.openConnection();

        int responseCode = requestURL.getResponseCode();
        System.out.println(responseCode);
        System.out.println(3);

        if (responseCode == HttpURLConnection.HTTP_OK) {
          JsonAdapter<List<String[]>> adapter = moshi.adapter(Types.newParameterizedType(List.class, String[].class));
          System.out.println(4);

          try (Buffer newBuffer = new Buffer().readFrom(requestURL.getInputStream())) {
            System.out.println(11);

            List<String[]> jsonResponse = adapter.fromJson(newBuffer);

            System.out.println(6);

            // get data from json
            String broadbandData = jsonResponse.get(1)[1];
            System.out.println(4);

            // response map w timestamp and data
            responseMap.put("timestamp", timestamp);
            responseMap.put("data", broadbandData);
            System.out.println(timestamp + broadbandData);
            System.out.println(responseMap);
            System.out.println(5);
            return adapter2.toJson(responseMap);
          } catch (Exception e) {
            System.out.println(e);
            responseMap.put("error_type", e);
            return adapter2.toJson(responseMap);
          }
          // parse json into map


        } else {
          // handle failed API request
          throw new DatasourceException("API request failed with response code: " + responseCode);
        }

      } catch (IOException e) {
        throw new DatasourceException(e.getMessage());
      } catch (Exception e) {
        responseMap.put("error_type", e);
        return adapter2.toJson(responseMap);
      }
    }
    responseMap.put("error_type", "Not found county or state");
    return adapter2.toJson(responseMap);
  }

  private String getDateTime() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date now = new Date();
    return dateFormat.format(now);
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
    } catch (Exception e) {
      System.out.println(e);
    }
    finally {
      connection.disconnect();
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
//              String sCode = row.get(1);
              String countyCode = row.get(2);
              // Split the full name to extract the county name
              String[] parts = fullName.split(",");
              if (parts.length >= 2) {
                String cName = parts[0].trim(); // Extracted county name

                // Check if the county name and state match the user's request
                if (countyName.equalsIgnoreCase(cName)) {
                  System.out.println(countyName + ": " + countyCode);

                  return countyCode;
                }
              }
            }

          }
        }
      } catch (Exception e) {
        System.out.println(e);
      }
      finally {
        connection.disconnect();
      }
    }

    // Return null if county name not found
    return null;
  }

}
