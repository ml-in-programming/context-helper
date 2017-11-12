package ru.spb.se.contexthelper.context.declr;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DeclarationsContext {
  @NotNull
  private final List<Declaration> declarations;

  DeclarationsContext(@NotNull List<Declaration> declarations) {
    this.declarations = declarations;
  }

  @NotNull
  public List<Declaration> getDeclarations() {
    return declarations;
  }
}