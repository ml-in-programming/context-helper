package ru.spb.se.contexthelper.ui;

import com.intellij.ui.components.JBScrollPane;
import ru.spb.se.contexthelper.lookup.StackExchangeQueryResults;
import ru.spb.se.contexthelper.model.ContextHelperTreeModel;

import javax.swing.*;
import java.awt.*;

/** ContextHelper's side panel. */
public class ContextHelperPanel extends JPanel {

  private final JTextField queryJTextField;
  private final ContextHelperTree tree;
  private ContextHelperTreeModel treeModel;

  public ContextHelperPanel() {
    this.treeModel = new ContextHelperTreeModel(null);
    this.queryJTextField = new JTextField("_placeholder_");
    this.tree = new ContextHelperTree(treeModel);
    buildGui();
  }

  /** Updates the underlying data model and JTree element. */
  public void updatePanelWithQueryResults(StackExchangeQueryResults queryResults) {
    queryJTextField.setText(queryResults.getQueryContent());
    treeModel = new ContextHelperTreeModel(queryResults.getQuestions());
    tree.setModel(treeModel);
  }

  /** Configures the panel's UI. */
  private void buildGui() {
    setLayout(new BorderLayout());
    add(queryJTextField, BorderLayout.PAGE_START);
    add(new JBScrollPane(tree), BorderLayout.CENTER);
  }
}
