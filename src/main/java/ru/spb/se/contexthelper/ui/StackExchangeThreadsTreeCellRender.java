package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Answer;
import com.google.code.stackexchange.schema.Comment;
import com.google.code.stackexchange.schema.Question;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StackExchangeThreadsTreeCellRender implements TreeCellRenderer {

  private static final String QUESTION_ICON_PATH = "/icons/so-icon.png";
  private static final String ANSWER_ICON_PATH = "/icons/so-icon.png";

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree, Object renderedObject, boolean selected, boolean expanded, boolean leaf,
      int row, boolean hasFocus) {
    JLabel label = new JLabel();
    if (renderedObject instanceof Question) {
      Question question = (Question) renderedObject;
      label.setText("<html>["
          + convertScoreToString(question.getScore()) + "] "
          + question.getTitle() +
          "</html>");
      label.setIcon(IconLoader.getIcon(QUESTION_ICON_PATH));
    } else if (renderedObject instanceof Answer) {
      Answer answer = (Answer) renderedObject;
      label.setText("<html>[" + convertScoreToString(answer.getScore()) + "] answer </html>");
      label.setIcon(IconLoader.getIcon(ANSWER_ICON_PATH));
    } else if (renderedObject instanceof Comment) {
      Comment comment = (Comment) renderedObject;
      label.setText("<html>[" + convertScoreToString(comment.getScore()) + "] comment </html>");
      label.setIcon(IconLoader.getIcon(ANSWER_ICON_PATH));
    }
    return label;
  }

  @VisibleForTesting
  static String convertScoreToString(long score) {
    NumberFormat numberFormat = new DecimalFormat("+#;-#");
    return numberFormat.format(score);
  }
}
