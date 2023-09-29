package edu.brown.cs.student.main.handlers;

/**
 * Required exception for broadband.
 */

public class DatasourceException extends Exception {
  private final Throwable cause;

  public DatasourceException(String message) {
    super(message);
    this.cause = null;
  }

  /**
   * Throwable datasource exception.
   * @param message message to display
   * @param cause cause that led to it
   */
  public DatasourceException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }
}
