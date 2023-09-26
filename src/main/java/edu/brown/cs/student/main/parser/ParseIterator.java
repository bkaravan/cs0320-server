package edu.brown.cs.student.main.parser;

import java.util.Iterator;
import java.util.List;

public class ParseIterator<T> implements Iterator {
  private List<T> data;
  private int ind;

  public ParseIterator(List<T> datalist) {
    this.data = datalist;
    this.ind = 0;
  }

  public boolean hasNext() {
    try {
      this.data.get(this.ind);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public T next() {
    T row = this.data.get(this.ind);
    this.ind++;
    return row;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
