package ru.spb.se.contexthelper.testing;

/**
 * Stands for the part of the dataset that should be used to evaluate the method.
 */
public enum DatasetEnum {

  DEV("dev"),
  TEST("test");

  private final String dirname;

  DatasetEnum(String dirname) {
    this.dirname = dirname;
  }

  public String getDirname() {
    return dirname;
  }
}