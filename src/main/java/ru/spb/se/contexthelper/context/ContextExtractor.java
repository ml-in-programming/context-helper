package ru.spb.se.contexthelper.context;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiMethodImpl;
import org.jetbrains.annotations.Nullable;
import ru.spb.se.contexthelper.util.ActionEventUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts a context from the context of {@link AnActionEvent}. The extracted context is later
 * used for building a request.
 */
public class ContextExtractor {

  private final AnActionEvent event;

  public ContextExtractor(AnActionEvent event) {
    this.event = event;
  }

  public EventContext extractContext() throws ContextExtractionException {
    Editor editor = ActionEventUtil.getEditorFor(event);
    if (editor == null) {
      throw new ContextExtractionException("Editor is not selected");
    }
    PsiFile psiFile = ActionEventUtil.getPsiFileFor(event);
    if (psiFile == null) {
      throw new ContextExtractionException("No enclosing file found");
    }
    PsiElement psiElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
    if (psiElement == null) {
      throw new ContextExtractionException("No PSI for the element found");
    }
    PsiElement methodPsiElement = findParentMethod(psiElement);
    if (methodPsiElement == null) {
      throw new ContextExtractionException("Current PSI element is not a part of any method");
    }
    List<PsiElement> psiElements = new ArrayList<>();
    traversePsiElement(methodPsiElement, psiElements);
    return new EventContext(psiElements);
  }

  /** Finds a PSI element that represents a method by checking element's parents. */
  @Nullable
  private static PsiElement findParentMethod(PsiElement psiElement) {
    if (psiElement == null) {
      return null;
    }
    return psiElement instanceof PsiMethodImpl
        ? psiElement
        : findParentMethod(psiElement.getParent());
  }

  /** Recursively iterates over element's children and collects traversed {@link PsiElement}s. */
  private static void traversePsiElement(
      PsiElement psiElement,
      List<PsiElement> psiElements) {
    traversePsiElement(psiElement, psiElements, 0);
  }

  private static void traversePsiElement(
      PsiElement psiElement, List<PsiElement> psiElements, int indentionLevel) {
    psiElements.add(psiElement);
    for (PsiElement element : psiElement.getChildren()) {
      traversePsiElement(element, psiElements, indentionLevel + 1);
    }
  }
}
