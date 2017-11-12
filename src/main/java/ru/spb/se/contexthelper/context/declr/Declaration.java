package ru.spb.se.contexthelper.context.declr;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/** Represents an extracted declaration statement. */
public class Declaration {
  @NotNull
  private final PsiElement psiElement;
  private final int declarationHolderLevel;

  Declaration(@NotNull PsiElement psiElement, int declarationHolderLevel) {
    this.psiElement = psiElement;
    this.declarationHolderLevel = declarationHolderLevel;
  }

  @NotNull
  public PsiElement getPsiElement() {
    return psiElement;
  }

  public int getDeclarationHolderLevel() {
    return declarationHolderLevel;
  }

  @Override
  public String toString() {
    return "Declaration{" +
        "psiElement=" + psiElement +
        ", declarationHolderLevel=" + declarationHolderLevel +
        '}';
  }
}