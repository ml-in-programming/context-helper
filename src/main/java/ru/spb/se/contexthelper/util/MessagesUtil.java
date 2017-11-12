package ru.spb.se.contexthelper.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class MessagesUtil {
  public static void showInfoDialog(@NotNull String message, @NotNull Project project) {
    showMessageDialog(project, message, "Information", Messages.getInformationIcon());
  }

  public static void showErrorDialog(@NotNull String message, @NotNull Project project) {
    showMessageDialog(project, message, "Error", Messages.getErrorIcon());
  }

  private static void showMessageDialog(
      @NotNull Project project,
      @NotNull String message,
      @NotNull String title,
      @NotNull Icon icon) {
    Messages.showMessageDialog(project, message, title, icon);
  }

  private MessagesUtil() {}
}