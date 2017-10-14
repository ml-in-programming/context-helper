package ru.spb.se.contexthelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;
import ru.spb.se.contexthelper.model.ContextHelperTreeModel;

import javax.swing.*;

/** ContextHelper's side panel. */
public class ContextHelperPanel extends JPanel {

  private final ContextHelperProjectComponent projectComponent;

  private final Project project;

  private final ContextHelperTree tree;
  private final ContextHelperTreeModel treeModel;
  private final JSplitPane splitPane;


  public ContextHelperPanel(ContextHelperProjectComponent projectComponent) {
    this.projectComponent = projectComponent;
    this.project = projectComponent.getProject();
    this.treeModel = new ContextHelperTreeModel(projectComponent);
    this.tree = new ContextHelperTree(treeModel);

    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JBScrollPane(tree), tree) {
      public void setDividerLocation(int location) {
        super.setDividerLocation(location);
      }
    };
    splitPane.setDividerLocation(200);
    add(splitPane);
  }
}
