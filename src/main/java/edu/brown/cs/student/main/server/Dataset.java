package edu.brown.cs.student.main.server;

import java.util.ArrayList;
import java.util.List;

public class Dataset<T> {
  private List<T> dataset = new ArrayList<T>();


  public void setDataset(List<T> data) {
    this.dataset = data;
  }

  public List<T> getDataset() {
    return new ArrayList<T>(this.dataset);
  }

}
