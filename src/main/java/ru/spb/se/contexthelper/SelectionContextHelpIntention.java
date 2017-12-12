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
import ru.spb.se.contexthelper.context.NotEnoughContextException;
import ru.spb.se.contexthelper.context.bag.SelectionContextExtractor;
import ru.spb.se.contexthelper.context.bag.SelectionContextQueryBuilder;
import ru.spb.se.contexthelper.context.bag.SelectionContext;
import ru.spb.se.contexthelper.util.MessagesUtil;

/** IntentionAction for getting help based on the currently selected code in the editor. */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class SelectionContextHelpIntention implements IntentionAction {
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
  public void invoke(
      @NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    SelectionContextExtractor contextExtractor = new SelectionContextExtractor(editor, file);
    SelectionContext context = contextExtractor.extractContext();
    SelectionContextQueryBuilder queryBuilder = new SelectionContextQueryBuilder(context);
    String query;
    try {
      query = queryBuilder.buildQuery();
    } catch (NotEnoughContextException ignored) {
      MessagesUtil.showInfoDialog("Unable to describe the context.", project);
      return;
    }
    ContextHelperProjectComponent helperComponent =
        ContextHelperProjectComponent.Companion.getFor(project);
    helperComponent.processQuery(query + " java", "bag_of_words");
  }
}