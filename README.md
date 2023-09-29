# Server

### Completed by Bohdan Karavan (bkaravan) and Jeffrey Tao (jtao25)
This project took about 16 hours to complete.
Here is the [Github repo](https://github.com/cs0320-f23/server-bkaravan-jtao25.git)

## About

This project creates a server application that provides a web API for data retrieval and search.
This server utilizes two primary data sources: CSV files and the United States Census's American 
Community Survey (ACS) API. The server processes incoming requests, fetches data from
the specified sources, and responds with the relevant information. In particular, this server aims
to help the Mesh Network Community Coalition with identifying potential areas with poor Internet access.

## Design Choices

### High-Level Design
**Handlers**

This project includes multiple handler classes responsible for processing different types of requests.
These handlers act as intermediaries between incoming HTTP requests and the server's core logic. Each
handler specializes in a specific task, ensuring modularity and extensibility.

`LoadHandler` deals with GET requests related to loading CSV files. It expects a "filepath" query parameter
specifying the path to the CSV file to be loaded. The `MyParser` class is used to parse the
CSV file, and the `CreatorFromRow` interface and custom `Creator` class are used to specify how rows
from the CSV file are transformed into lists of strings. Upon successful loading, it updates the dataset in
the `Dataset` object, and if an error occurs during loading, it generates a JSON response indicating the failure.

`ViewHandler` handles GET requests for viewing the contents of a loaded CSV. It takes in `Dataset`
as a parameter, serializes the data into a JSON response, and provides this data in the `view data` field
of the response. If the dataset is empty, it returns an error response indicating that no files are loaded.

`SearchHandler` is responsible for handling GET requests for searching the loaded CSV data. It takes in 
`Dataset` as a parameter along with query parameters for the search. It uses the provided query parameters
to perform a search operation (using the `MySearcher` class) on the dataset, constructs a JSON response
containing the results, and handles errors by returning the appropriate messages in JSON.

Lastly, `BroadbandHandler` is responsible for interacting with the ACS API to retrieve data related to broadband access. 
It fetches this data based on user-provided state and county names, which are used to send a query to the API
to get their corresponding numerical codes, which are then used to make another request to 
retrieve the actual broadband data. 

All four of these handler classes implement the `Route` Spark interface in order to create mappings between 
the HTTP request paths and their corresponding handler actions. When a user sends a GET request to a specific 
URL, the framework will route that request to the appropriate `Route` implementation.

**Server**

The `Server` class acts as the central component that listens for incoming HTTP requests, routes them
to the appropriate handler, and sends back the corresponding responses using the SparkJava framework.
Endpoints associated with each handler—`loadcsv`, `viewcsv`, `searchcsv`, and `broadband`— are set up 
here. `current` is a `Dataset Object` that stores data loaded from CSV files, which is passed to the `LoadHandler`,
`ViewHandler`, and `SearchHandler` for processing.

**Note:** We did not get to User Story 3: Caching. 

### Data Structures and Runtime?

We created a `Dataset` class that serves as a central data structure for storing the CSV data that is loaded into the server,
using a nested list structure `List<List<String>>`to represent the data. `LoadHandler`, `ViewHandler`, and
`SearchHandler` take in `Dataset` to access the loaded CSV for processing and responding to their respective requests.

## Bugs and Testing
**Bugs**

At the time of writing this README, no bugs are observed. 

**Testing**


## Usage and How-To
### Developer POV

To run this program, run the `Server` class and navigate to the server URL in the Terminal.
It should say:
```
Server started at http://localhost:3232
```

**Load, View, and Search**

In your browser URL, you can then start making API requests to load, view, or search the contents
of a CSV file by calling the `loadcsv`, `viewcsv`, or `searchcsv` endpoints.

The `loadcsv` API query for CSV data takes a file path. After the `loadcsv` endpoint, input the following:
`?filepath=<your_file_path>`. For example, to load the test.csv file in this program, the URL would look like
this: 
```
http://localhost:3232/loadcsv?filepath=data/csvtest/test.csv. 
```
At most one CSV file can be loaded at any time.
Using `viewcsv` or `searchcsv` CSV queries without a CSV loaded produces an error API response.

Once you have loaded a CSV file, use the `viewcsv` query to view the entire CSV file's contents as a JSON:
```
http://localhost:3232/viewcsv
```

The `searchcsv` API query takes several parameters that you must provide: `search`, `header`, and `narrow`. 

`search` is the search term, which can look like:
- class, Brown, string, Something-Something
- the matching is CASE SENSITIVE - if the file contains the word Brown, and the user looks for brown, it WILL NOT match
- the matching is NOT WHITESPACE SENSITIVE - if the file contains " Brown", and you look for "Brown", it WILL match
- the spaces in-between the words for row entries are NOT removed - "Brown Uni" will still be "Brown Uni"

`header` is a boolean for whether that file contains a header line and can look like:
- TRUE, true, True - all of these will mark that there IS a header in the CSV file
- Anything else will prompt the searcher to think that there is no header

`narrow` is not required, but if provided make sure to specify the search in the given input, whether it is
an index or a column name:
- "Nam: SomeName" for the column-name search, "Ind: 0" for the column-index search
- MAKE SURE to use the quotes on this input, otherwise it won't correctly process it
- Any permutation of NAM, nam, ind, IND should work
- Make sure that 0 <= the number after IND: < the number of entries in the first row
- If the column name is not matched with any entry of the first row, the search will default to searching the whole dataset for the match with the search word

For example, with the test.csv file loaded, try 
```
/searchcsv?search=jake&narrow="Ind: 0"&header=true
```
This will send back rows matching the given search criteria.

**Broadband**

The `broadband` API query requests must have state and county query parameters.
For example, to retrieve the broadband data for Lassen County, California, the request
would look like: 
```
/broadband?state=California&county=Lassen County
```

Queries are not sensitive to case. However, make sure to include "county" after the actual county name 
in the county argument. An input of just `"Lassen"` would produce an error; it
must be `"Lassen County"`.

