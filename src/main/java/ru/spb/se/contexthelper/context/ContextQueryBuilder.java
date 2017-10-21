package ru.spb.se.contexthelper.context;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiMethodImpl;

import java.util.Arrays;
import java.util.List;

public class ContextQueryBuilder {

  private final EventContext context;

  public ContextQueryBuilder(EventContext context) {
    this.context = context;
  }

  /** Builds query by splitting parent's method name. */
  public String buildQuery() {
    List<PsiElement> psiElements = context.getPsiElements();
    PsiMethodImpl parentMethod = (PsiMethodImpl) psiElements.get(0);
    String[] queryWords = parentMethod.getName().split("(?=\\p{Upper})");
    return Arrays.stream(queryWords).reduce("", (s1, s2) -> s1 + " " + s2);
  }
}
