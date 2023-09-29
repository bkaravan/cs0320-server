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
import java.util.HashMap;
import java.util.Map;
import okio.Buffer;
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

    // Set the Spark port number. This can only be done once, and has to
    // happen before any route maps are added. Hence using @BeforeClass.
    // Setting port 0 will cause Spark to use an arbitrary available port.
    Spark.port(0);
    // Don't try to remember it. Spark won't actually give Spark.port() back
    // until route mapping has started. Just get the port number later. We're using
    // a random _free_ port to remove the chances that something is already using a
    // specific port on the system used for testing.

    // Remove the logging spam during tests
    //   This is surprisingly difficult. (Notes to self omitted to avoid complicating things.)

    // SLF4J doesn't let us change the logging level directly (which makes sense,
    //   given that different logging frameworks have different level labels etc.)
    // Changing the JDK *ROOT* logger's level (not global) will block messages
    //   (assuming using JDK, not Log4J)
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
    Spark.unmap("viewcsv");
    Spark.unmap("searchcsv");
    Spark.unmap("broadband");
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
    public String Loaded;
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

    System.out.println(response.Loaded);
    System.out.println(response.result);

    clientConnection.disconnect();
    assertEquals("success", response.result);
    assertEquals("data/stars/stardata.csv", response.Loaded);
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

  // Recall that the "throws IOException" doesn't signify anything but acknowledgement to the type checker
//  public void testAPIViewCSVFile() throws IOException {
//    // HttpURLConnection clientConnection = tryRequest("loadCensus?state=Maine");
//    HttpURLConnection clientConnection0 = tryRequest("loadCSV?file=data/stars/ten-star.csv");
//
//    HttpURLConnection clientConnection = tryRequest("viewCSV");
//
//    // Get an OK response (the *connection* worked, the *API* provides an error response)
//    assertEquals(200, clientConnection.getResponseCode());
//
//    // Now we need to see whether we've got the expected Json response.
//    // SoupAPIUtilities handles ingredient lists, but that's not what we've got here.
//    Moshi moshi = new Moshi.Builder().build();
//    // We'll use okio's Buffer class here
//    ResponseLoadCSV response0 =
//        moshi.adapter(ResponseLoadCSV.class).fromJson(new Buffer().readFrom(clientConnection0.getInputStream()));
//    ResponseLoadCSV response =
//        moshi.adapter(ResponseLoadCSV.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
//
//    System.out.println(response);
//    // ^ If that succeeds, we got the expected response. Notice that this is *NOT* an exception, but a real Json reply.
//
//    clientConnection.disconnect();
//    assertEquals("Error: Invalid or empty file name",response);
//
//  }

}
