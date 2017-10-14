package ru.spb.se.contexthelper.ui;

import com.google.code.stackexchange.schema.Question;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class ContextHelperTreeCellRender implements TreeCellRenderer {

  private static final String QUESTION_ICON_PATH = "/icons/so-icon.png";

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
      int row, boolean hasFocus) {
    JLabel label = new JLabel();
    if (value instanceof Question) {
      Question question = (Question) value;
      label.setText("<html>" + question.getTitle() + "</html>");
      label.setIcon(IconLoader.getIcon(QUESTION_ICON_PATH));
    }
    return label;
  }
}
