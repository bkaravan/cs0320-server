package edu.brown.cs.student.main.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import okio.Buffer;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * The BroadbandHandler class handles HTTP requests related to broadband data retrieval. It
 * communicates with an external ACS API to fetch data on broadband access for a user-specified
 * state and county. Implements the `Route` Spark interface in order to create a mapping between the
 * HTTP request path.
 */
public class BroadbandHandler implements Route {

  /**
   * Method that handles an HTTP request to fetch broadband data for a specified state and county.
   * This method communicates with an external ACS API to retrieve broadband access statistics, and
   * constructs an HTTP response containing the retrieved data or appropriate error messages.
   *
   * @param request the HTTP request containing query parameters for state and county.
   * @param response the HTTP response to be populated with broadband data or error messages.
   * @return null, as the data or error messages are added to the HTTP response.
   * @throws Exception if an error occurs during data retrieval or processing.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    // get date and time
    String timestamp = getDateTime();
    // setup our moshi to send the response back to the user
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> responseAdapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();
    // request parameters
    String stateName = request.queryParams("state");
    String countyName = request.queryParams("county");
    if (stateName == null) {
      responseMap.put("type", "error");
      responseMap.put("error_type", "bad_request");
      return responseAdapter.toJson(responseMap);
    }
    if (countyName == null) {
      responseMap.put("type", "error");
      responseMap.put("error_type", "bad_request");
      return responseAdapter.toJson(responseMap);
    }
    // retrieve code for state and county
    String stateCode = getStateCode(stateName);
    String countyCode = getCountyCode(stateCode, countyName);

    // if we got both the statecode and the county code
    if (!stateCode.equals("null") && !countyCode.equals("null")) {

      try {
        // set up the URL
        String apiUrl =
            "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_001E&for=county:"
                + countyCode
                + "&in=state:"
                + stateCode;

        URL url = new URL(apiUrl);

        HttpURLConnection requestURL = (HttpURLConnection) url.openConnection();

        int responseCode = requestURL.getResponseCode();
        // check that the response was good
        if (responseCode == HttpURLConnection.HTTP_OK) {
          // deserialize the response
          JsonAdapter<List<String[]>> adapter =
              moshi.adapter(Types.newParameterizedType(List.class, String[].class));
          // try to read from the file and get the data
          try (Buffer newBuffer = new Buffer().readFrom(requestURL.getInputStream())) {

            List<String[]> jsonResponse = adapter.fromJson(newBuffer);

            // get data from json, it is the row of index one, second entry for broadband access
            // data
            String broadbandData = jsonResponse.get(1)[1];

            // fill our response map with correct data and send it back
            responseMap.put("result", "success");
            responseMap.put("state", stateName);
            responseMap.put("county", countyName);
            responseMap.put("timestamp", timestamp);
            responseMap.put("broadband_access", broadbandData);
            return responseAdapter.toJson(responseMap);
          } catch (Exception e) {
            responseMap.put("error", "error_bad_json");
            return responseAdapter.toJson(responseMap);
          }
        } else {
          // handle failed API request
          throw new DatasourceException("API request failed with response code: " + responseCode);
        }
      } catch (IOException e) {
        throw new DatasourceException(e.getMessage());
      } catch (Exception e) {
        responseMap.put("error_type", e);
        return responseAdapter.toJson(responseMap);
      }
    }
    if (stateCode.equals("null")) {
      responseMap.put("no_state", stateName);
    }
    if (countyCode.equals("null")) {
      responseMap.put("no_county", countyName);
    }
    responseMap.put("error_type", "error_bad_request");
    return responseAdapter.toJson(responseMap);
  }

  /**
   * Helper method that retrieves the current date and time of the request in a specific format.
   *
   * @return a formatted string representing the current date and time.
   */
  private String getDateTime() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date now = new Date();
    return dateFormat.format(now);
  }

  /**
   * Helper method that makes an API request to retrieve the state code based on the provided state
   * name. Returns null if state name not found.
   *
   * @param stateName the name of the state for which to retrieve the code.
   * @return the state code corresponding to the provided state name.
   * @throws IOException if an error occurs during the API request.
   */
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
        JsonAdapter<List<List<String>>> adapter =
            moshi.adapter(Types.newParameterizedType(List.class, List.class));

        // parse the json response into a List
        List<List<String>> jsonResponse =
            adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));

        // iterate through the list to find the state code for the target state name
        for (List<String> row : jsonResponse) {
          if (row.size() >= 2) {
            String name = row.get(0);
            String code = row.get(1);

            if (stateName.equalsIgnoreCase(name)) {
              //              System.out.println(stateName + ": " + code);
              return code;
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      connection.disconnect();
    }
    return "null";
  }

  /**
   * Helper method that makes an API request to retrieve the county code based on the provided state
   * code and county name. Returns null if county name not found.
   *
   * @param stateCode the state code for the target state.
   * @param countyName the name of the county for which to retrieve the code.
   * @return the county code corresponding to the provided state code and county name.
   * @throws IOException if an error occurs during the API request.
   */
  private String getCountyCode(String stateCode, String countyName) throws IOException {
    // api request to get county code based on county name and state code
    if (stateCode != null) {
      String apiUrl =
          "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode;

      HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();

      try {
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
          Moshi moshi = new Moshi.Builder().build();
          JsonAdapter<List<List<String>>> adapter =
              moshi.adapter(Types.newParameterizedType(List.class, List.class));

          // Parse the JSON response into a List
          List<List<String>> jsonResponse =
              adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));

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
                  //                  System.out.println(countyName + ": " + countyCode);

                  return countyCode;
                }
              }
            }
          }
        }
      } catch (Exception e) {
        System.out.println(e);
      } finally {
        connection.disconnect();
      }
    }

    // Return null if county name not found
    return "null";
  }
}
