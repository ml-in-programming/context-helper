package ru.spb.se.contexthelper.context.declr;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class ContextPsiScopeProcessor implements PsiScopeProcessor {
  @NotNull
  private final List<Declaration> declarations = new ArrayList<>();
  private int declarationHolderLevel = 0;

  @NotNull
  List<Declaration> getDeclarations() {
    return declarations;
  }

  @Override
  public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
    declarations.add(new Declaration(element, declarationHolderLevel));
    return true;
  }

  @Nullable
  @Override
  public <T> T getHint(@NotNull Key<T> hintKey) {
    return null;
  }

  @Override
  public void handleEvent(@NotNull Event event, @Nullable Object associated) {
    if (event == Event.SET_DECLARATION_HOLDER) {
      declarationHolderLevel++;
    }
  }
}