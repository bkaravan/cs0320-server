package edu.brown.cs.testing;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.handlers.BroadbandHandler;
import edu.brown.cs.student.main.handlers.LoadHandler;
import edu.brown.cs.student.main.handlers.SearchHandler;
import edu.brown.cs.student.main.handlers.ViewHandler;
import edu.brown.cs.student.main.server.Dataset;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okio.Buffer;
import org.eclipse.jetty.io.ssl.ALPNProcessor.Client;
import org.eclipse.jetty.util.IO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the actual handlers.
 * <p>
 * On Sprint 2, you'll need to deserialize API responses. Although this demo doesn't need to do
 * that, these _tests_ do.
 * <p>
 * https://junit.org/junit5/docs/current/user-guide/
 * <p>
 * Because these tests exercise more than one "unit" of code, they're not "unit tests"...
 * <p>
 * If the backend were "the system", we might call these system tests. But I prefer "integration
 * test" since, locally, we're testing how the Soup functionality connects to the handler. These
 * distinctions are sometimes fuzzy and always debatable; the important thing is that these ARE NOT
 * the usual sort of unit tests.
 * <p>
 * Note: if we were doing this for real, we might want to test encoding formats other than UTF-8
 * (StandardCharsets.UTF_8).
 */

class TestingServer {

  @BeforeAll
  public static void setup_before_everything() {

    Spark.port(0);

    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  /**
   * Shared state for all tests. We need to be able to mutate it (adding recipes etc.) but never
   * need to replace the reference itself. We clear this state out after every test runs.
   */


  @BeforeEach
  public void setup() {
    // Re-initialize state, etc. for _every_ test method run

    // In fact, restart the entire Spark server for every test!
    Dataset csvData = new Dataset();
    Spark.get("loadcsv", new LoadHandler(csvData));
    Spark.get("viewcsv", new ViewHandler(csvData));
    Spark.get("searchcsv", new SearchHandler(csvData));
    Spark.get("broadband", new BroadbandHandler());

    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
  }

  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("loadcsv");
    Spark.unmap("/viewcsv");
    Spark.unmap("/searchcsv");
    Spark.unmap("/broadband");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint (NOTE: this would be better if it had more
   *                structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  static private HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    //clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }


  public static class SuccessResponseLoadCSV {
    public String result;
    public String loaded;

  }

  @Test
  // Recall that the "throws IOException" doesn't signify anything but acknowledgement to the type checker
  public void testLoadCSVSuccess() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/stars/stardata.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
//    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
//    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);

    SuccessResponseLoadCSV response = moshi.adapter(SuccessResponseLoadCSV.class).
        fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

//    System.out.println(response.Loaded);
//    System.out.println(response.result);

    clientConnection.disconnect();
    assertEquals("success", response.result);
    assertEquals("data/stars/stardata.csv", response.loaded);
  }

  public static class FailResponseLoadCSV {

    public String response_type;
  }

  @Test
  // Recall that the "throws IOException" doesn't signify anything but acknowledgement to the type checker
  public void testAPILoadCSVFileError() throws IOException {
    // HttpURLConnection clientConnection = tryRequest("loadCensus?state=Maine");
    HttpURLConnection clientConnection = tryRequest(
        "loadcsv?filepath=data/stars/stardataFALSE.csv");

    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    FailResponseLoadCSV response =
        moshi.adapter(FailResponseLoadCSV.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("error_datasource: data/stars/stardataFALSE.csv", response.response_type);
  }

  public static class MissingFilepath {


    public String error_type;
    public String missing_argument;
  }

  @Test
  // Recall that the "throws IOException" doesn't signify anything but acknowledgement to the type checker
  public void testLoadCSVMissingFilepath() throws IOException {
    HttpURLConnection clientConnection = tryRequest(
        "loadcsv");

    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    MissingFilepath response =
        moshi.adapter(MissingFilepath.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("missing_argument", response.error_type);
    assertEquals("filepath", response.missing_argument);
  }

  public static class ViewSuccessResponse {
    public String result;
    public List<List<String>> viewData;
  }

  @Test
  public void testViewCSVSuccess() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("viewcsv");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    ViewSuccessResponse response = moshi.adapter(ViewSuccessResponse.class).
        fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("success", response.result);
//    System.out.println(response.viewData);
    List<String> check = new ArrayList<>();
    check.add("name");
    check.add("class");
    check.add("position");
    assertEquals(check, response.viewData.get(0));
  }

  public static class ViewNoFileResponse {
    public String type;
    public String error_type;
  }

  @Test
  public void testViewNoFileLoaded() throws IOException {
    HttpURLConnection clientConnection = tryRequest("viewcsv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    ViewNoFileResponse response = moshi.adapter(ViewNoFileResponse.class).
        fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("error", response.type);
    assertEquals("No files are loaded", response.error_type);
  }

  @Test
  public void testSearchNoFileLoaded() throws IOException {
    HttpURLConnection clientConnection = tryRequest("searchcsv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    ViewNoFileResponse response = moshi.adapter(ViewNoFileResponse.class).
        fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("error", response.type);
    assertEquals("No files are loaded", response.error_type);
  }

  public static class SearchMissingArgResponse {
    public String type;
    public String error_type;
    public String error_arg;
  }

  @Test
  public void testSearchCSVMissingArgSearch() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchMissingArgResponse response = moshi.adapter(SearchMissingArgResponse.class).
        fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("error", response.type);
    assertEquals("missing_parameter", response.error_type);
    assertEquals("search", response.error_arg);
  }

  @Test
  public void testSearchCSVMissingArgHeader() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv?search=left");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchMissingArgResponse response = moshi.adapter(SearchMissingArgResponse.class).
        fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("error", response.type);
    assertEquals("missing_parameter", response.error_type);
    assertEquals("header", response.error_arg);
  }

  public static class SearchFoundResponse {
    public String result;
    public List<List<String>> view_data;
  }

  @Test
  public void searchCSVFoundAllArgs() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv?search=right&header=true&ind:2");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchFoundResponse response = moshi.adapter(SearchFoundResponse.class).
        fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("success", response.result);
    List<String> check = new ArrayList<>();
    check.add("jake");
    check.add("second");
    check.add("right");
    assertEquals(check, response.view_data.get(0));
  }







}
