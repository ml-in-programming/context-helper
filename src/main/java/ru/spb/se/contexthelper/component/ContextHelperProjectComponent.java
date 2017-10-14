package ru.spb.se.contexthelper.component;

import com.intellij.openapi.components.NamedComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import ru.spb.se.contexthelper.ui.ContextHelperPanel;

import static ru.spb.se.contexthelper.ContextHelperConstants.ID_TOOL_WINDOW;
import static ru.spb.se.contexthelper.ContextHelperConstants.PLUGIN_NAME;

/** Component which is called to initialize ContextHelper plugin for each {@link Project}. */
public class ContextHelperProjectComponent implements ProjectComponent {

  /** Last part of the name for {@link NamedComponent}. */
  private static final String COMPONENT_NAME = "ContextHelperProjectComponent";

  private static final String ICON_PATH_TOOL_WINDOW = "/icons/binary-tree-icon.png";

  private final Project project;

  private ContextHelperPanel viewerPanel;

  public ContextHelperProjectComponent(Project project) {
    this.project = project;
  }

  public Project getProject() {
    return project;
  }

  @Override
  public void projectOpened() {
    viewerPanel = new ContextHelperPanel(this);
    ToolWindow toolWindow = getToolWindow();
    toolWindow.setIcon(IconLoader.getIcon(ICON_PATH_TOOL_WINDOW));
  }

  private ToolWindow getToolWindow() {
    ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ID_TOOL_WINDOW);
    if (toolWindow == null) {
      toolWindow =
          toolWindowManager.registerToolWindow(ID_TOOL_WINDOW, true, ToolWindowAnchor.RIGHT);
      Content content = ContentFactory.SERVICE.getInstance().createContent(viewerPanel, "", false);
      toolWindow.getContentManager().addContent(content);
    }
    return toolWindow;
  }

  @Override
  public void projectClosed() {
  }

  @Override
  public void initComponent() {
  }

  @Override
  public void disposeComponent() {
  }

  @NotNull
  @Override
  public String getComponentName() {
    return PLUGIN_NAME + "." + COMPONENT_NAME;
  }
}
