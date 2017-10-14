package ru.spb.se.contexthelper.ui;

import com.intellij.psi.PsiElement;
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;
import ru.spb.se.contexthelper.model.ContextHelperTreeModel;

import javax.swing.*;
import java.awt.*;

/** ContextHelper's side panel. */
public class ContextHelperPanel extends JPanel {

  private final ContextHelperProjectComponent projectComponent;

  private final ContextHelperTree tree;
  private ContextHelperTreeModel treeModel;

  public ContextHelperPanel(ContextHelperProjectComponent projectComponent) {
    this.projectComponent = projectComponent;
    this.treeModel = new ContextHelperTreeModel(projectComponent);
    this.tree = new ContextHelperTree(treeModel);
    buildGui();
  }

  /** Updates the underlying data model and JTree element. */
  public void updatePanelForRootElement(PsiElement rootPsiElement) {
    treeModel = new ContextHelperTreeModel(projectComponent);
    treeModel.setRootPsiElement(rootPsiElement);
    tree.setModel(treeModel);
  }

  /** Configures the panel's UI. */
  private void buildGui() {
    setLayout(new BorderLayout());
    add(tree);
  }
}
