package ru.spb.se.contexthelper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import ru.spb.se.contexthelper.context.declr.Declaration;
import ru.spb.se.contexthelper.context.declr.DeclarationsContext;
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor;
import ru.spb.se.contexthelper.util.ActionEventUtil;

public class DeclarationsContextHelpAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = ActionEventUtil.getProjectFor(event);
    if (project == null) {
      return;
    }
    Editor editor = ActionEventUtil.getEditorFor(event);
    if (editor == null) {
      showInfoMessage("Editor is not selected", project);
      return;
    }
    PsiFile psiFile = ActionEventUtil.getPsiFileFor(event);
    if (psiFile == null) {
      showInfoMessage("No enclosing file found", project);
      return;
    }
    PsiElement psiElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
    if (psiElement == null) {
      showInfoMessage("No PSI for the element found", project);
      return;
    }
    DeclarationsContextExtractor contextExtractor = new DeclarationsContextExtractor();
    DeclarationsContext context = contextExtractor.extractContextFor(psiElement);
    for (Declaration declaration : context.getDeclarations()) {
      System.out.println(declaration);
    }
  }

  private static void showInfoMessage(@NotNull String message, @NotNull Project project) {
    Messages.showMessageDialog(
        project,
        message,
        "Information",
        Messages.getInformationIcon());
  }
}