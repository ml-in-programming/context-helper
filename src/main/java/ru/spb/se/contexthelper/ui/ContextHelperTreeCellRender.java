package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Question;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class ContextHelperTreeCellRender implements TreeCellRenderer {

  private static final String QUESTION_ICON_PATH = "/icons/so-icon.png";
  private static final String ANSWER_ICON_PATH = "/icons/so-icon.png";

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
      int row, boolean hasFocus) {
    JLabel label = new JLabel();
    if (value instanceof Question) {
      Question question = (Question) value;
      label.setText("<html>" + question.getTitle() + "</html>");
      label.setIcon(IconLoader.getIcon(QUESTION_ICON_PATH));
    } else if (value instanceof Answer) {
      Answer answer = (Answer) value;
      label.setText(
          "<html>" +
          "[" + renderScore(answer.getScore()) + "]" +
          " by " + answer.getOwner().getDisplayName() +
          " on " + answer.getCreationDate() +
          "</html>");
      label.setIcon(IconLoader.getIcon(ANSWER_ICON_PATH));
    }
    return label;
  }

  /** Renders {@link Long} sign correctly. */
  private static String renderScore(long score) {
    if (score > 0) {
      return "+" + score;
    } else {
      return String.valueOf(score);
    }
  }
}
