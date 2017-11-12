package ru.spb.se.contexthelper.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionEventUtil {
  @Nullable
  public static Project getProjectFor(@NotNull AnActionEvent event) {
    return event.getData(PlatformDataKeys.PROJECT);
  }

  @Nullable
  public static Editor getEditorFor(@NotNull AnActionEvent event) {
    return event.getData(PlatformDataKeys.EDITOR);
  }

  @Nullable
  public static PsiFile getPsiFileFor(@NotNull AnActionEvent event) {
    return event.getData(LangDataKeys.PSI_FILE);
  }

  private ActionEventUtil() {}
}