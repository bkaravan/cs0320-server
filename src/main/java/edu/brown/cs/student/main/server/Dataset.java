package edu.brown.cs.student.main.server;

import java.util.ArrayList;
import java.util.List;

public class Dataset {
  private List<List<String>> dataset = new ArrayList<>();


  public void setDataset(List<List<String>> data) {
    this.dataset = data;
  }

  public List<List<String>> getDataset() {
    return this.dataset;
  }

}
