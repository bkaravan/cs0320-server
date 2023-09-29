package edu.brown.cs.student.main.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Dataset is a class that serves as a central data structure to store the CSV data that
 * is loaded into the server, using a nested list structure, List<List<String>>, to represent the data.
 * LoadHandler, ViewHandler, and SearchHandler take in Dataset to access the loaded CSV for processing
 * and responding to their respective requests.
 */
public class Dataset {
  private List<List<String>> dataset = new ArrayList<>();

  /**
   * Sets the dataset to the provided list of lists of strings.
   *
   * @param data The new dataset (list of list of strings) to set.
   */
  public void setDataset(List<List<String>> data) {
    this.dataset = data;
  }

  /**
   * Retrieves the current dataset stored in this container.
   *
   * @return The list of lists of strings representing the dataset.
   */
  public List<List<String>> getDataset() {
    return this.dataset;
  }

}
