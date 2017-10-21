package ru.spb.se.contexthelper.context;

import com.intellij.psi.PsiElement;

import java.util.List;

/** Builds query from the {@link EventContext}. */
public class ContextQueryBuilder {

  private final EventContext context;

  public ContextQueryBuilder(EventContext context) {
    this.context = context;
  }

  public String buildQuery() {
    List<PsiElement> psiElements = context.getPsiElements();
    for (PsiElement psiElement : psiElements) {
      if (psiElement.getTextLength() != 0) {
        return psiElement.getText();
      }
    }
    return "";
  }
}
