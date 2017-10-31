package ru.spb.se.contexthelper.context;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts a context from the context of current {@link PsiFile} and its {@link Editor}. The
 * extracted context is later used for building a request.
 */
public class ContextExtractor {

  private final PsiFile psiFile;

  private final int selectionStartOffset;

  private final int selectionEndOffset;

  public ContextExtractor(Editor editor, PsiFile psiFile) {
    this.selectionStartOffset = editor.getSelectionModel().getSelectionStart();
    this.selectionEndOffset = editor.getSelectionModel().getSelectionEnd();
    this.psiFile = psiFile;
  }

  public EventContext extractContext() {
    List<PsiElement> psiElements = new ArrayList<>();
    traversePsiElement(psiFile, psiElements);
    return new EventContext(psiElements);
  }

  private void traversePsiElement(PsiElement element, List<PsiElement> selectedElements) {
    int elementStart = element.getTextOffset();
    int elementEnd = elementStart + element.getTextLength();
    if (selectionStartOffset <= elementStart && elementEnd <= selectionEndOffset) {
      selectedElements.add(element);
    }
    for (PsiElement childElement : element.getChildren()) {
      traversePsiElement(childElement, selectedElements);
    }
  }
}
