package ru.spb.se.contexthelper.context.declr;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DeclarationsContextExtractor {
  @NotNull
  public DeclarationsContext extractContextFor(@NotNull PsiElement psiElement) {
    ContextPsiScopeProcessor scopeProcessor = new ContextPsiScopeProcessor();
    PsiElement currentPsiElement = psiElement;
    PsiElement previousPsiElement = psiElement;
    while (!(currentPsiElement instanceof PsiDirectory)) {
      currentPsiElement.processDeclarations(
          scopeProcessor, ResolveState.initial(), previousPsiElement, psiElement);
      previousPsiElement = currentPsiElement;
      currentPsiElement = currentPsiElement.getParent();
    }
    List<Declaration> declarations = scopeProcessor.getDeclarations();
    return new DeclarationsContext(declarations);
  }
}