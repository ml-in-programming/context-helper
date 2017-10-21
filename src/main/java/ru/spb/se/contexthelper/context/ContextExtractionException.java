package ru.spb.se.contexthelper.context;

/**
 * Exception thrown by {@link ContextExtractor} if context extraction cannot be executed
 * properly.
 */
public class ContextExtractionException extends Exception {

  ContextExtractionException(String message) {
    super(message);
  }
}
