# CSV Parser
### Completed by Bohdan Karavan (bkaravan)
This project took about 15 hours to complete.
Here is the [Github repo](https://github.com/cs0320-f23/csv-bkaravan)

## About

This project creates a parser that can look through the Comma-Separated-Value files and create a
database. That database is a list of generic type `T`, and can later be passed into a searcher
object that can search the database for a given word. It will then return the list of rows where it
found a match.

## Design Choices

### High-Level Design
This project is centered around two classes that are located in their respective packages: `MyParser`
and `MySearcher`. `MyParser` is a custom parser that takes in a `Reader Object` (in this implementation
it's mostly `StringReader` or `FileReader`), and a class that implements the `CreatorFromRow` interface.
The big upside for this is as long as a developer creates a class that implements `CreatorFromRow`,
they should be used to use `MyParser` without error. `MyParser` reads lines using a given reader, and
uses a Regular Expression pattern to split the lines into a list of strings, that is passed into
a method of the class that implements `CreatorFromRow`. The returning value of generic type `T` is
added to the `ArrayList<T>` that is called the database. In this project, there are two
examples of classes that implement the `CreatorFromRow` interface. The one that is used in this
implementation of `MySearcher` is called `RowHandler`. Its create method takes in a row and feeds it
into an `ArrayList<String>`. While doing so, it strips the inputs of the row of whitespaces, and
whenever it encounters an empty entry, the row will not be parsed and the according message with
`FactoryFailureException` will be printed. Another example used to test the create method testing
is a class called `SecondRowHandler`. It operates using a custom class, `ParsedRow`, which combines the 
index of the row and its contents. Respectively for the two RowHandlers, `MyParser` will create either
as their dataset
- `ArrayList<ArrayList<String>>`
- `ArrayList<ParsedRow>`

`MySearcher` is a custom searcher that takes in the dataset, boolean statement for the presence of the
header line, and any specifications for the search by the user. The correct usage of these arguments
will be described in the **How-to...** section. Depending on the constructor parameters, the searcher
will set up its private fields to make the search optimized according to user's wishes (which
includes looking at the column name, index of the column, or neither). After it's constructed, the
`toFind()` method is called, which takes in a string that a user is looking for. It will create a
record of every row where a match was found, which can later be accessed via the `getFound()` method.

**Note:** if the header is present in the csv, the search will only look for matches starting with 
the first data row (indexed 1 in that case).

### Data Structures and Runtime
The runtime for `MyParser` is worst-case linear with respect to the number of rows in the CSV file. The
choice of the appropriate dataStructure to store the data was between some HashMap or a
List of generics, and my choice was the latter, specifically the `ArrayList`. That way it creates an
easy to navigate dataset for search, which also allows to optimize when the index is specified.

The runtime for `MySearcher` is worst-case O(M * N), where M is the number of rows in the dataset
from MyParser, and N is their length. When the user specifies the index of the desired column,
whether by stating the column name or index, the runtime should be O(M), because the access to the
desired input in each row is O(1). The way `MySearcher` stores found answers (as rows) is also
implemented with an ArrayList. The decision was made to allow for potential control of how many
rows the user would want to print, but due to the time constraints this feature was not implemented.


## Bugs and Testing
At the time of writing this Readme, no bugs are observed in the program, which is definitely not 
to say there aren't any. For the sake of time and example, most of the testing for searching was 
run on smaller CSV files whose data is fairly clean. The testing included checking the correct 
implementation of every class, correct representation of datasets, different search permutation 
with and without header files, or index specifications. `TestingRegEx` class consists of a few tests
for the RegEx that was provided, along with a few of my own that allowed me to catch some potential
problems. The tests that "caught" something interesting in RegEx are also present in `TestingParseSearch`.

Exceptions that are throwable are FileNotFind, depending on the reader usage. IOException, if something
goes wrong during file reading, and FacturyFailureException, if a row doesn't get parsed. The user
is protected from providing an invalid input, and the program should print out an error message 
and terminate if that is every the case. 
All the tests are present and should be runnable in the testing class.

### Checkstyle
There are some warnings throughout the code that mainly scream at regular expression, but the 
checkstyle looks good. 

## Usage and How-To

### User POV
**Note**: user pov implies the usage of both parser and searcher. 

There should be two ways to run this script. The first one would require going to the `Main`
class, and navigate to _Edit Configurations_ tab. In there, in the _Program Arguments_ line, the 
user needs to provide at least **three** arguments: path to the csv file, the word in that file 
the user is trying to match with, and whether that file contains a header line. Here is an input
example:

**PATH** can look like:
- relative from the root: _data/stars/stardata.csv_
- absolute: _C:\Users\karav\OneDrive\Documents\CS32\csv-bkaravan\data\census\income_by_race_edited.csv_

**SEARCH WORD** can look like:
- class, Brown, string, Something-Something
- the matching is CASE SENSITIVE - if the file contains the word Brown, and the user looks for brown, it WILL NOT match
- the matching is NOT WHITESPACE SENSITIVE - if the file contains " Brown", and you look for "Brown", it WILL match
- the spaces in-between the words for row entries are NOT removed - "Brown Uni" will still be "Brown Uni"

**HEADER LINE** can look like:
- TRUE, true, True - all of these will mark that there IS a header in the CSV file
- Anything else will prompt the searcher to think that there is no header

If the user does not provide some of these inputs, the program will ask you to retry. 

The last input is not required, but if provided will make sure to specify the search in the given
input, whether it is an index or a column name. 

**LAST INPUT LOOKS LIKE**
- "Nam: SomeName" for the column-name search, "Ind: 0" for the column-index search
- MAKE SURE to use the quotes on this input, otherwise it won't correctly process it
- Any permutation of NAM, nam, ind, IND should work
- Make sure that 0 <= the number after IND: < the number of entries in the first row
- If the column name is not matched with any entry of the first row, the search will default to searching the whole dataset for the match with the search word

The program should output a list of rows where it found the match. Please remember that if a row 
had an empty entry, it wouldn't be parsed so the searcher won't find any matches there!

The second way of using the script involves running **./run** with the above-mentioned arguments on the 
root directory of this project, but it's not working quite well at the moment. 

Thank you for using and good luck!

### Developer POV

As long as the developer has some implementation of `CreatorFromRow` interface, `MyParser` should be 
able to work. To create an instance of this object, pass in any `Reader` object and the previously 
mentioned interface to the constructor. Then, call the method `toParse()` on that object. Depending on the implementation 
of `CreatorFromRow`, the developer can expect different outcomes when `FactoryFailureException` is thrown. 
At the moment, `toParse()` is set up in a way that will print out the index of the row that was not 
parsed, and the according `FactoryFailureException` after that. However, the parser will not 
terminate and keep going through the document, feeling the dataset with rows that were successfully 
parsed. The data structure for the dataset is `ArrayList<T>`, and can be accessed via `getDataset()`
getter. Happy using!