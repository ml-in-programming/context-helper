package ru.spb.se.contexthelper.component

import com.google.code.stackexchange.schema.StackExchangeSite
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.ui.components.JBList
import com.intellij.ui.content.ContentFactory
import ru.spb.se.contexthelper.ContextHelperConstants.ID_TOOL_WINDOW
import ru.spb.se.contexthelper.ContextHelperConstants.PLUGIN_NAME
import ru.spb.se.contexthelper.context.ContextProcessor
import ru.spb.se.contexthelper.context.NotEnoughContextException
import ru.spb.se.contexthelper.lookup.QueryRecommender
import ru.spb.se.contexthelper.lookup.StackExchangeClient
import ru.spb.se.contexthelper.lookup.StackExchangeQuestionResults
import ru.spb.se.contexthelper.lookup.StackOverflowGoogleSearchClient
import ru.spb.se.contexthelper.ui.ContextHelperPanel
import ru.spb.se.contexthelper.util.MessagesUtil
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

/** Component which is called to initialize ContextHelper plugin for each [Project]. */
class ContextHelperProjectComponent(val project: Project) : ProjectComponent {
    val stackExchangeClient = StackExchangeClient(STACK_EXCHANGE_API_KEY, STACK_EXCHANGE_SITE)
    private val googleSearchClient = StackOverflowGoogleSearchClient(GOOGLE_SEARCH_API_KEY)
    private val queryRecommender: QueryRecommender = QueryRecommender()

    private var viewerPanel: ContextHelperPanel? = null

    override fun getComponentName(): String = PLUGIN_NAME + "." + COMPONENT_NAME

    override fun projectOpened() {
        initToolWindow()
        queryRecommender.loadQueries(QUERIES_PATH)
    }

    override fun projectClosed() {
        disposeToolWindow()
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


    private fun disposeToolWindow() {
        viewerPanel = null
        if (isToolWindowRegistered()) {
            ToolWindowManager.getInstance(project).unregisterToolWindow(ID_TOOL_WINDOW)
        }
    }

    fun assistAround(psiElement: PsiElement, editor: Editor) {
        val contextProcessor = ContextProcessor(psiElement)
        val query = try {
            contextProcessor.generateQuery()
        } catch (ignored: NotEnoughContextException) {
            MessagesUtil.showInfoDialog("Unable to describe the context.", project)
            return
        }
        val questionList =
            JBList<String>(queryRecommender.relevantQuestions(query, QUESTS_SUGGEST_COUNT))
        val popupWindow =
            JBPopupFactory.getInstance().createListPopupBuilder(questionList)
                .setTitle("Select query for StackOverflow")
                .setMovable(false)
                .setResizable(false)
                .setRequestFocus(true)
                .setItemChoosenCallback {
                    val selectedQuery = questionList.selectedValue
                    if (selectedQuery != null) {
                        processQuery(selectedQuery + " java")
                    }
                }.createPopup()
        popupWindow.showInBestPositionFor(editor)
    }

    fun processQuery(query: String) {
        LOG.info("processQuery($query)")
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
                LOG.error(e)
            }
            indicator.stop()
        }
    }

    companion object {
        private val LOG = Logger.getInstance(
            "ru.spb.se.contexthelper.component.ContextHelperProjectComponent")

        /** Last part of the name for {@link NamedComponent}. */
        private val COMPONENT_NAME = "ContextHelperProjectComponent"
        private val ICON_PATH_TOOL_WINDOW = "/icons/se-icon.png"

        private val QUERIES_PATH = "/tasks/suggested.txt"
        private val QUESTS_SUGGEST_COUNT = 5

        private val STACK_EXCHANGE_API_KEY = "F)x9bhGombhjqpnXt)5Mwg(("
        private val STACK_EXCHANGE_SITE = StackExchangeSite.STACK_OVERFLOW

        private val GOOGLE_SEARCH_API_KEY = "AIzaSyBXQg39PaVjqONPEL4eubyA7S-pEuqVKOc"

        fun getFor(project: Project): ContextHelperProjectComponent =
            project.getComponent(ContextHelperProjectComponent::class.java)
    }
}