package ru.spb.se.contexthelper.context;

import com.intellij.psi.PsiElement;

import java.util.List;

/** Represents a context extracted by {@link ContextExtractor}. */
public class EventContext {

  private final List<PsiElement> psiElements;

  EventContext(List<PsiElement> psiElements) {
    this.psiElements = psiElements;
  }

  public List<PsiElement> getPsiElements() {
    return psiElements;
  }
}
