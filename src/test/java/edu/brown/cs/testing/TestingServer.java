package edu.brown.cs.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.handlers.BroadbandHandler;
import edu.brown.cs.student.main.handlers.LoadHandler;
import edu.brown.cs.student.main.handlers.SearchHandler;
import edu.brown.cs.student.main.handlers.ViewHandler;
import edu.brown.cs.student.main.server.Dataset;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * Test the actual handlers.
 *
 * <p>On Sprint 2, you'll need to deserialize API responses. Although this demo doesn't need to do
 * that, these _tests_ do.
 *
 * <p>https://junit.org/junit5/docs/current/user-guide/
 *
 * <p>Because these tests exercise more than one "unit" of code, they're not "unit tests"...
 *
 * <p>If the backend were "the system", we might call these system tests. But I prefer "integration
 * test" since, locally, we're testing how the Soup functionality connects to the handler. These
 * distinctions are sometimes fuzzy and always debatable; the important thing is that these ARE NOT
 * the usual sort of unit tests.
 *
 * <p>Note: if we were doing this for real, we might want to test encoding formats other than UTF-8
 * (StandardCharsets.UTF_8).
 */
class TestingServer {

  @BeforeAll
  public static void setup_before_everything() {

    Spark.port(0);

    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  /**
   * Shared state for all tests. We create a setup for spark to see our handlers, and also
   * set up our mocked data for test usage
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

    /// MOCK SETUP ///
    Dataset current = new Dataset();
    List<List<String>> exampledata = new ArrayList<>();
    String[] array1 = {"first", "Second", "third"};
    String[] array2 = {"first1", "Second2", "third3"};
    ArrayList<String> example1 = new ArrayList<>(Arrays.asList(array1));
    ArrayList<String> example2 = new ArrayList<>(Arrays.asList(array2));
    exampledata.add(example1);
    exampledata.add(example2);
    current.setDataset(exampledata);
    Spark.get("loadcsv2", new LoadHandler(current));
    Spark.get("viewcsv2", new ViewHandler(current));
    Spark.get("searchcsv2", new SearchHandler(current));

    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
  }

  /**
   * Teardown and stop Spark after each test
   */

  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on both endpoints
    Spark.unmap("/loadcsv");
    Spark.unmap("/viewcsv");
    Spark.unmap("/searchcsv");
    Spark.unmap("/broadband");
    Spark.unmap("/loadcsv2");
    Spark.unmap("/viewcsv2");
    Spark.unmap("/searchcsv2");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint (NOTE: this would be better if it had more
   *     structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    // clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * A class to store information about the successful response from Loading.
   */

  public static class SuccessResponseLoadCSV {
    public String result;
    public String loaded;
  }

  /**
   * Test to check if we are able to successfully load a CSV with a header.
   * @throws IOException
   */

  @Test
  // Recall that the "throws IOException" doesn't signify anything but acknowledgement to the type
  // checker
  public void testLoadCSVSuccessWithHeader() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/stars/stardata.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SuccessResponseLoadCSV response =
        moshi
            .adapter(SuccessResponseLoadCSV.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("success", response.result);
    assertEquals("data/stars/stardata.csv", response.loaded);
  }

  /**
   * Test to see if we are able to successfully load CSV without a header.
   * @throws IOException
   */

  @Test
  // Recall that the "throws IOException" doesn't signify anything but acknowledgement to the type
  // checker
  public void testLoadCSVSuccessNoHeader() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filepath=data/csvtest/noHeaderTest.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SuccessResponseLoadCSV response =
        moshi
            .adapter(SuccessResponseLoadCSV.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    clientConnection.disconnect();
    assertEquals("success", response.result);
    assertEquals("data/csvtest/noHeaderTest.csv", response.loaded);
  }

  /**
   * Class to store a failed loading response.
   */
  public static class FailResponseLoadCSV {

    public String response_type;
  }

  /**
   * Test to see how we handle an incorrect path.
   * @throws IOException
   */

  @Test
  // Recall that the "throws IOException" doesn't signify anything but acknowledgement to the type
  // checker
  public void testAPILoadCSVFileError() throws IOException {
    // HttpURLConnection clientConnection = tryRequest("loadCensus?state=Maine");
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filepath=data/stars/stardataFALSE.csv");

    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    FailResponseLoadCSV response =
        moshi
            .adapter(FailResponseLoadCSV.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("error_datasource: data/stars/stardataFALSE.csv", response.response_type);
  }

  /**
   * Class to store data about calling load without proper parameters
   */

  public static class MissingFilepath {

    public String error_type;
    public String missing_argument;
  }

  /**
   * Test for loading without providing a filepath.
   * @throws IOException
   */
  @Test
  // Recall that the "throws IOException" doesn't signify anything but acknowledgement to the type
  // checker
  public void testLoadCSVMissingFilepath() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv");

    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    MissingFilepath response =
        moshi
            .adapter(MissingFilepath.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("missing_argument", response.error_type);
    assertEquals("filepath", response.missing_argument);
  }

  /**
   * Class to store successful response to view after loading
   */
  public static class ViewSuccessResponse {
    public String result;
    public List<List<String>> viewData;
  }

  /**
   * Test for successful viewing
   * @throws IOException
   */
  @Test
  public void testViewCSVSuccess() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("viewcsv");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    ViewSuccessResponse response =
        moshi
            .adapter(ViewSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

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

  /**
   * Class to store information about Viewing Errors.
   */

  public static class ViewNoFileResponse {
    public String type;
    public String error_type;
  }

  /**
   * Test to view without prior loading.
   * @throws IOException
   */
  @Test
  public void testViewNoFileLoaded() throws IOException {
    HttpURLConnection clientConnection = tryRequest("viewcsv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    ViewNoFileResponse response =
        moshi
            .adapter(ViewNoFileResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("error", response.type);
    assertEquals("No files are loaded", response.error_type);
  }

  /**
   * Testing search without prior loading
   * @throws IOException
   */

  @Test
  public void testSearchNoFileLoaded() throws IOException {
    HttpURLConnection clientConnection = tryRequest("searchcsv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    ViewNoFileResponse response =
        moshi
            .adapter(ViewNoFileResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("error", response.type);
    assertEquals("No files are loaded", response.error_type);
  }

  /**
   * Class to store an error response to improper arguments of search
   */
  public static class SearchMissingArgResponse {
    public String type;
    public String error_type;
    public String error_arg;
  }

  /**
   * Test for search without a required argument search
   * @throws IOException
   */

  @Test
  public void testSearchCSVMissingArgSearch() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchMissingArgResponse response =
        moshi
            .adapter(SearchMissingArgResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("error", response.type);
    assertEquals("missing_parameter", response.error_type);
    assertEquals("search", response.error_arg);
  }

  /**
   * Test for search without the required argument header
   * @throws IOException
   */
  @Test
  public void testSearchCSVMissingArgHeader() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv?search=left");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchMissingArgResponse response =
        moshi
            .adapter(SearchMissingArgResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("error", response.type);
    assertEquals("missing_parameter", response.error_type);
    assertEquals("header", response.error_arg);
  }

  /**
   * Class to store response for found Search.
   */
  public static class SearchFoundResponse {
    public String result;
    public List<List<String>> view_data;
  }

  /**
   * Test that search runs when Provided all arguments (index)
   * @throws IOException
   */
  @Test
  public void searchCSVFoundAllArgs() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv?search=right&header=true&ind:2");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchFoundResponse response =
        moshi
            .adapter(SearchFoundResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("success", response.result);
    List<String> check = new ArrayList<>();
    check.add("jake");
    check.add("second");
    check.add("right");
    assertEquals(check, response.view_data.get(0));
  }

  /**
   * Test that search runs when Provided all arguments (Name)
   * @throws IOException
   */
  @Test
  public void searchCSVFoundAllArgsName() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv?search=right&header=true&nam:position");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchFoundResponse response =
        moshi
            .adapter(SearchFoundResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("success", response.result);
    List<String> check = new ArrayList<>();
    check.add("jake");
    check.add("second");
    check.add("right");
    assertEquals(check, response.view_data.get(0));
  }

  /**
   * Test searching for correctly finding without the third argument.
   * @throws IOException
   */
  @Test
  public void searchCSVFoundNoNarrow() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/csvtest/test.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv?search=right&header=true");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchFoundResponse response =
        moshi
            .adapter(SearchFoundResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("success", response.result);
    List<String> check = new ArrayList<>();
    check.add("alex");
    check.add("first");
    check.add("right");
    assertEquals(check, response.view_data.get(1));
  }

  /**
   * Class to store a response when search ran but didn't find anything.
   */

  public static class SearchNotFoundResponse {
    public String type;
    public String error_type;
    public String search_word;
    public String specifier;
  }

  /**
   * Testing for looking for a word that is not there
   * @throws IOException
   */
  @Test
  public void searchCSVNotFound() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/stars/ten-star.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv?search=HELLO&header=true");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchNotFoundResponse response =
        moshi
            .adapter(SearchNotFoundResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("error", response.type);
    assertEquals("no match found", response.error_type);
    assertEquals("HELLO", response.search_word);
  }

  /**
   * Testing to see if we specify the index where we look for, the searchword is present in the
   * document but is not in that index, and the searcher doesn't find it
   * @throws IOException
   */
  @Test
  public void searchCSVNotFoundAllArgs() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=data/stars/ten-star.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 =
        tryRequest("searchcsv?search=Sol&header=true&narrow=ind:2");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchNotFoundResponse response =
        moshi
            .adapter(SearchNotFoundResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("error", response.type);
    assertEquals("no match found", response.error_type);
    assertEquals("Sol", response.search_word);
    assertEquals("ind:2", response.specifier);
  }

  /**
   * Test for searcher to find things without a header
   * @throws IOException
   */
  @Test
  public void searchCSVFoundNoHeader() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("loadcsv?filepath=data/csvtest/noHeaderTest.csv");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection2 = tryRequest("searchcsv?search=Barus&header=false");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchFoundResponse response =
        moshi
            .adapter(SearchFoundResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection.disconnect();
    clientConnection2.disconnect();
    assertEquals("success", response.result);
    List<String> check = new ArrayList<>();
    check.add("Carla");
    check.add("Barus");
    check.add("32");
    assertEquals(check, response.view_data.get(0));
  }

  /**
   * Class to store good responses to broadband searches.
   */

  public static class BroadbandSuccess {
    public String result;
    public String state;
    public String county;
    public String broadband_access;
  }

  /**
   * Testing for correctly finding information for provided state and county
   * @throws IOException
   */
  @Test
  public void broadbandFound() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("broadband?state=Rhode%20Island&county=Providence%20County");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    BroadbandSuccess response =
        moshi
            .adapter(BroadbandSuccess.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("success", response.result);
    assertEquals("Rhode Island", response.state);
    assertEquals("Providence County", response.county);
    assertEquals("92.8", response.broadband_access);
  }

  /**
   * Class to store failed responses from broadband
   */
  public static class BroadbandFail {
    public String type;
    public String no_county;
    public String no_state;
    public String error_type;
  }

  /**
   * Testing to handle incorrect county spelling
   * @throws IOException
   */
  @Test
  public void broadbandNoCountyFound() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("broadband?state=Rhode%20Island&county=Pravidence%20County");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    BroadbandFail response =
        moshi
            .adapter(BroadbandFail.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("error_bad_request", response.error_type);
    assertEquals("Pravidence County", response.no_county);
  }

  /**
   * Testing to handle incorrect state spelling
   * @throws IOException
   */
  @Test
  public void broadbandNoStateFound() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("broadband?state=Rhade%20Island&county=Pravidence%20County");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    BroadbandFail response =
        moshi
            .adapter(BroadbandFail.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("error_bad_request", response.error_type);
    assertEquals("Rhade Island", response.no_state);
  }

  /**
   * Testing for broadband without arguments
   * @throws IOException
   */
  @Test
  public void broadbandNoArgsFound() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    BroadbandFail response =
        moshi
            .adapter(BroadbandFail.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    clientConnection.disconnect();
    assertEquals("bad_request", response.error_type);
    assertEquals("error", response.type);
  }

  ////////////////////// MOCKS and UNITS /////////////////////////////////////
  // In our implementation, it is really challenging to mock how broadbandhandler works
  // So, instead, we mocked how our custom handlers work

  /**
   * Mock with a local json file
   * @throws IOException
   */
  @Test
  public void testViewNoFileLoadedMock() throws IOException {
    String json = new String(Files.readAllBytes(Paths.get("data/Mocks/mocktest1.json")));
    Moshi moshi = new Moshi.Builder().build();

    JsonAdapter<ViewNoFileResponse> adapter = moshi.adapter(ViewNoFileResponse.class);
    ViewNoFileResponse response = adapter.fromJson(json);

    assertEquals("error", response.type);
    assertEquals("No files are loaded", response.error_type);
  }

  /**
   * Mock the view handler
   * @throws IOException
   */
  @Test
  public void testViewFileLoadedMocked() throws IOException {
    HttpURLConnection clientConnection2 = tryRequest("viewcsv2");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    ViewSuccessResponse response =
        moshi
            .adapter(ViewSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection2.disconnect();
    assertEquals("success", response.result);
    List<String> ex = new ArrayList<>();
    ex.add("first");
    ex.add("Second");
    ex.add("third");
    assertEquals(ex, response.viewData.get(0));
  }

  /**
   * Mock the search when we give it all arguments
   * @throws IOException
   */
  @Test
  public void testSearchMockedDatasourceFoundAllArgs() throws IOException {
    HttpURLConnection clientConnection2 =
        tryRequest("searchcsv2?search=Second2&header=false&narrow=ind:1");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchFoundResponse response =
        moshi
            .adapter(SearchFoundResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection2.disconnect();
    assertEquals("success", response.result);
    List<String> check = new ArrayList<>();
    check.add("first1");
    check.add("Second2");
    check.add("third3");
    assertEquals(check, response.view_data.get(0));
  }

  /**
   * Mocking for search that successfully finds with two arguments
   * @throws IOException
   */
  @Test
  public void testSearchMockedDatasourceFound2Args() throws IOException {
    HttpURLConnection clientConnection2 = tryRequest("searchcsv2?search=first1&header=false");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchFoundResponse response =
        moshi
            .adapter(SearchFoundResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection2.disconnect();
    assertEquals("success", response.result);
    List<String> check = new ArrayList<>();
    check.add("first1");
    check.add("Second2");
    check.add("third3");
    assertEquals(check, response.view_data.get(0));
  }

  /**
   * mock searching and looking for the word that is not there.
   * @throws IOException
   */
  @Test
  public void testSearchMockedDatasourceNotFound() throws IOException {
    HttpURLConnection clientConnection2 = tryRequest("searchcsv2?search=NOTHERE&header=false");
    assertEquals(200, clientConnection2.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();

    SearchNotFoundResponse response =
        moshi
            .adapter(SearchNotFoundResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

    clientConnection2.disconnect();
    assertEquals("error", response.type);
    assertEquals("no match found", response.error_type);
    assertEquals("NOTHERE", response.search_word);
  }
}
