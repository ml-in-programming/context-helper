package ru.spb.se.contexthelper.context.bag;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts a context from the context of current {@link PsiFile} and its {@link Editor}. The
 * extracted context is later used for building a request.
 */
public class SelectionContextExtractor {

  private final PsiFile psiFile;

  private final int selectionStartOffset;

  private final int selectionEndOffset;

  public SelectionContextExtractor(Editor editor, PsiFile psiFile) {
    this.selectionStartOffset = editor.getSelectionModel().getSelectionStart();
    this.selectionEndOffset = editor.getSelectionModel().getSelectionEnd();
    this.psiFile = psiFile;
  }

  public SelectionContext extractContext() {
    List<PsiElement> psiElements = new ArrayList<>();
    traversePsiElement(psiFile, psiElements);
    return new SelectionContext(psiElements);
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
