package ru.spb.se.contexthelper.component

import com.google.code.stackexchange.schema.StackExchangeSite
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import ru.spb.se.contexthelper.ContextHelperConstants.ID_TOOL_WINDOW
import ru.spb.se.contexthelper.ContextHelperConstants.PLUGIN_NAME
import ru.spb.se.contexthelper.lookup.StackExchangeClient
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults
import ru.spb.se.contexthelper.lookup.StackOverflowGoogleSearchClient
import ru.spb.se.contexthelper.ui.ContextHelperPanel
import ru.spb.se.contexthelper.util.MessagesUtil
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

/** Component which is called to initialize ContextHelper plugin for each [Project]. */
class ContextHelperProjectComponent(val project: Project) : ProjectComponent {
    val stackExchangeClient =
        StackExchangeClient(STACK_EXCHANGE_API_KEY, STACK_EXCHANGE_SITE)

    private val googleSearchClient = StackOverflowGoogleSearchClient(GOOGLE_SEARCH_API_KEY)

    private var viewerPanel: ContextHelperPanel? = null

    override fun projectOpened() {
        initToolWindow()
    }

    private fun initToolWindow() {
        viewerPanel = ContextHelperPanel(this)
        val toolWindow = getOrRegisterToolWindow()
        toolWindow.icon = IconLoader.getIcon(ICON_PATH_TOOL_WINDOW)
    }

    private fun getOrRegisterToolWindow(): ToolWindow {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        return if (isToolWindowRegistered()) {
            ToolWindowManager.getInstance(project).getToolWindow(ID_TOOL_WINDOW)
        } else {
            val toolWindow =
                toolWindowManager.registerToolWindow(ID_TOOL_WINDOW, true, ToolWindowAnchor.RIGHT)
            val content = ContentFactory.SERVICE.getInstance().createContent(viewerPanel, "", false)
            toolWindow.contentManager.addContent(content)
            toolWindow
        }
    }

    private fun isToolWindowRegistered(): Boolean =
        ToolWindowManager.getInstance(project).getToolWindow(ID_TOOL_WINDOW) != null

    override fun projectClosed() {
        disposeToolWindow()
    }

    private fun disposeToolWindow() {
        viewerPanel = null
        if (isToolWindowRegistered()) {
            ToolWindowManager.getInstance(project).unregisterToolWindow(ID_TOOL_WINDOW)
        }
    }

    override fun getComponentName(): String = PLUGIN_NAME + "." + COMPONENT_NAME

    fun processQuery(query: String) {
        val contextHelperPanel = viewerPanel!!
        val indicator = object : EmptyProgressIndicator() {
            override fun start() {
                SwingUtilities.invokeLater {
                    contextHelperPanel.setQueryingStatus(true)
                }
            }

            override fun stop() {
                SwingUtilities.invokeLater {
                    contextHelperPanel.setQueryingStatus(false)
                }
            }
        }
        thread(isDaemon = true) {
            indicator.start()
            try {
                val questionIds = googleSearchClient.lookupQuestionIds(query)
                if (questionIds.isEmpty()) {
                    SwingUtilities.invokeLater {
                        MessagesUtil.showInfoDialog(
                            "No help available for the selected context.", project)
                    }
                } else {
                    // Currently using Google Custom Search. But it has 100 queries per day limit.
                    // May return to StackExchange search in the future.
                    // StackExchangeQuestionResults queryResults =
                    //     stackExchangeClient.requestRelevantQuestions(query);
                    val questions = stackExchangeClient.requestQuestionsWith(questionIds)
                    val queryResults = StackExchangeQuestionResults(query, questions)

                    SwingUtilities.invokeLater {
                        contextHelperPanel.updatePanelWithQueryResults(queryResults)
                    }
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    MessagesUtil.showErrorDialog("Unable to process the query.", project)
                }
            }
            indicator.stop()
        }
    }

    companion object {
        /** Last part of the name for {@link NamedComponent}. */
        val COMPONENT_NAME = "ContextHelperProjectComponent"
        val ICON_PATH_TOOL_WINDOW = "/icons/se-icon.png"

        val STACK_EXCHANGE_API_KEY = "F)x9bhGombhjqpnXt)5Mwg(("
        val STACK_EXCHANGE_SITE = StackExchangeSite.STACK_OVERFLOW

        val GOOGLE_SEARCH_API_KEY = "AIzaSyBXQg39PaVjqONPEL4eubyA7S-pEuqVKOc"

        fun getFor(project: Project): ContextHelperProjectComponent =
            project.getComponent(ContextHelperProjectComponent::class.java)
    }
}