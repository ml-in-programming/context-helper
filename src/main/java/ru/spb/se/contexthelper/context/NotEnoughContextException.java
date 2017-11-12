package ru.spb.se.contexthelper.context;

/** Exception is thrown if the query can not be built because of the lack of context. */
public class NotEnoughContextException extends RuntimeException {
  public NotEnoughContextException(String message) {
    super(message);
  }
}