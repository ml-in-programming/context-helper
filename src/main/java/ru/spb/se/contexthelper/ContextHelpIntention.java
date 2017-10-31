package ru.spb.se.contexthelper;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import ru.spb.se.contexthelper.component.ContextHelperProjectComponent;
import ru.spb.se.contexthelper.context.ContextExtractor;
import ru.spb.se.contexthelper.context.ContextQueryBuilder;
import ru.spb.se.contexthelper.context.EventContext;

/** IntentionAction for getting help based on the currently selected code in the editor. */
public class ContextHelpIntention implements IntentionAction {

  @Nls
  @NotNull
  @Override
  public String getText() {
    return "Get help based on the selection";
  }

  @Nls
  @NotNull
  @Override
  public String getFamilyName() {
    return "ContextHelper";
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    // Only Java language is supported as of now.
    return file.getLanguage() == JavaLanguage.INSTANCE && editor.getSelectionModel().hasSelection();
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    ContextExtractor contextExtractor = new ContextExtractor(editor, file);
    EventContext eventContext = contextExtractor.extractContext();
    ContextQueryBuilder contextQueryBuilder = new ContextQueryBuilder(eventContext);
    String query = contextQueryBuilder.buildQuery();
    ContextHelperProjectComponent helperProjectComponent =
        ContextHelperProjectComponent.getInstance(project);
    helperProjectComponent.processQuery(query);
  }
}
