package ru.spb.se.contexthelper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import ru.spb.se.contexthelper.context.declr.Declaration;
import ru.spb.se.contexthelper.context.declr.DeclarationsContext;
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor;
import ru.spb.se.contexthelper.util.ActionEventUtil;
import ru.spb.se.contexthelper.util.MessagesUtil;

public class DeclarationsContextHelpAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = ActionEventUtil.getProjectFor(event);
    if (project == null) {
      return;
    }
    Editor editor = ActionEventUtil.getEditorFor(event);
    if (editor == null) {
      MessagesUtil.showInfoDialog("Editor is not selected", project);
      return;
    }
    PsiFile psiFile = ActionEventUtil.getPsiFileFor(event);
    if (psiFile == null) {
      MessagesUtil.showInfoDialog("No enclosing file found", project);
      return;
    }
    PsiElement psiElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
    if (psiElement == null) {
      MessagesUtil.showInfoDialog("No PSI for the element found", project);
      return;
    }
    DeclarationsContextExtractor contextExtractor = new DeclarationsContextExtractor();
    DeclarationsContext context = contextExtractor.extractContextFor(psiElement);
    for (Declaration declaration : context.getDeclarations()) {
      System.out.println(declaration);
    }
  }
}