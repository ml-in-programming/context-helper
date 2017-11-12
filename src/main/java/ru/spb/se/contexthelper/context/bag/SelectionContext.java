package ru.spb.se.contexthelper.context.bag;

import com.intellij.psi.PsiElement;

import java.util.List;

/** Represents a context extracted by {@link SelectionContextExtractor}. */
public class SelectionContext {

  private final List<PsiElement> psiElements;

  SelectionContext(List<PsiElement> psiElements) {
    this.psiElements = psiElements;
  }

  List<PsiElement> getPsiElements() {
    return psiElements;
  }
}
