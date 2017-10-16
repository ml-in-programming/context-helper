package ru.spb.se.contexthelper.ui;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

import static ru.spb.se.contexthelper.ui.StackExchangeThreadsTreeCellRender.convertScoreToString;

public class StackExchangeThreadsTreeCellRenderTest {

  @Test
  public void convertPositiveScoreToString() throws Exception {
    String formatted = convertScoreToString(19);
    assertThat(formatted).isEqualTo("+19");
  }

  @Test
  public void convertZeroScoreToString() throws Exception {
    String formatted = convertScoreToString(0);
    assertThat(formatted).isEqualTo("+0");
  }

  @Test
  public void convertNegativeScoreToString() throws Exception {
    String formatted = convertScoreToString(-736);
    assertThat(formatted).isEqualTo("-736");
  }
}
