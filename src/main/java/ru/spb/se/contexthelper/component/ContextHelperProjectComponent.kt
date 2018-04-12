package ru.spb.se.contexthelper.component

import com.google.code.stackexchange.schema.StackExchangeSite
import com.intellij.openapi.application.PermanentInstallationID
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.ui.content.ContentFactory
import ru.spb.se.contexthelper.ContextHelperConstants.ID_TOOL_WINDOW
import ru.spb.se.contexthelper.ContextHelperConstants.PLUGIN_NAME
import ru.spb.se.contexthelper.context.NotEnoughContextException
import ru.spb.se.contexthelper.context.Query
import ru.spb.se.contexthelper.context.processor.ContextProcessorMethod
import ru.spb.se.contexthelper.context.processor.GCSContextProcessor
import ru.spb.se.contexthelper.context.processor.TypeNodeIndexContextProcessor
import ru.spb.se.contexthelper.lookup.*
import ru.spb.se.contexthelper.reporting.LocalUsageCollector
import ru.spb.se.contexthelper.reporting.StatsCollector
import ru.spb.se.contexthelper.ui.ContextHelperPanel
import ru.spb.se.contexthelper.util.showErrorDialog
import ru.spb.se.contexthelper.util.showInfoDialog
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

/** Component which is called to initialize ContextHelper plugin for each [Project]. */
class ContextHelperProjectComponent(val project: Project) : ProjectComponent {
    private val questionLookupClient: QuestionLookupClient = GoogleSearchCrawler()
//        GoogleCustomSearchClient(GOOGLE_SEARCH_API_KEY)
    private val stackExchangeClient =
        StackExchangeClient(STACK_EXCHANGE_API_KEY, STACK_EXCHANGE_SITE)
    private val threadsRecommenderClient = ThreadsRecommenderClient()

    private val statsCollector: StatsCollector = StatsCollector()

    private var sessionID: String = ""
    private val usageCollector: LocalUsageCollector = LocalUsageCollector(localSeverHostName)

    private var viewerPanel: ContextHelperPanel = ContextHelperPanel(this)
    private val questionResultsListeners: ArrayList<QuestionResultsListener> = arrayListOf()

    private var processorMethod: ContextProcessorMethod = ContextProcessorMethod.values().first()

    init {
        addResultsListener(object : QuestionResultsListener {
            override fun receiveResults(questionResults: StackExchangeQuestionResults): Boolean {
                SwingUtilities.invokeLater {
                    viewerPanel.updatePanelWithQueryResults(questionResults)
                }
                return true
            }
        })
    }

    fun addResultsListener(questionResultsListener: QuestionResultsListener) {
        questionResultsListeners.add(questionResultsListener)
    }

    fun changeProcessorMethodTo(processorMethod: ContextProcessorMethod) {
        this.processorMethod = processorMethod
    }

    override fun getComponentName(): String = "$PLUGIN_NAME.$COMPONENT_NAME"

    override fun projectOpened() {
        val toolWindow = getOrRegisterToolWindow()
        toolWindow.icon = IconLoader.getIcon(ICON_PATH_TOOL_WINDOW)
    }

    override fun projectClosed() {
        statsCollector.flush()
        statsCollector.ensureSent()
        if (isToolWindowRegistered()) {
            ToolWindowManager.getInstance(project).unregisterToolWindow(ID_TOOL_WINDOW)
        }
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

    /** @throws NotEnoughContextException if the context is not rich enough for the help. */
    fun assistAround(psiElement: PsiElement): Unit = when (processorMethod) {
        ContextProcessorMethod.GoogleSearchMethod -> {
            val gcsContextProcessor = GCSContextProcessor(psiElement)
            val textQuery = gcsContextProcessor.generateQuery()
            processTextQuery(textQuery)
        }
        ContextProcessorMethod.TypeNodeIndexMethod -> {
            val indexedTypesContextProcessor = TypeNodeIndexContextProcessor(psiElement)
            val query = indexedTypesContextProcessor.generateQuery()
            processQuery(query)
        }
    }

    fun processTextQuery(query: String) {
        process(query) {
            // Currently using Google Custom Search. But it has 100 queries per day limit.
            // May return to StackExchange search in the future.
            // StackExchangeQuestionResults queryResults =
            //     stackExchangeClient.requestRelevantQuestions(query);
            questionLookupClient.lookupQuestionIds(query)
        }
    }

    private fun processQuery(query: Query) {
        process(query.keywords.joinToString(" ") { it.word }) {
            threadsRecommenderClient.askForRecommendedThreads(query)
        }
    }

    private fun process(textQuery: String, idProducers: () -> List<Long>) {
        LOG.info("processQuery($textQuery)")
        val contextHelperPanel = viewerPanel
        thread(isDaemon = true) {
            SwingUtilities.invokeLater {
                contextHelperPanel.setQueryingStatus(true)
            }
            try {
                val questionIds = idProducers()
                if (questionIds.isEmpty()) {
                    SwingUtilities.invokeLater {
                        showInfoDialog("No help available for the selected context.", project)
                    }
                } else {
                    val queryResults =
                        stackExchangeClient.getQuestionsWithIds(textQuery, questionIds)
                    if (queryResults.questions.isEmpty()) {
                        SwingUtilities.invokeLater {
                            showInfoDialog(
                                "No matching StackOverflow questions were found.", project)
                        }
                    } else {
                        val iterator = questionResultsListeners.iterator()
                        while (iterator.hasNext()) {
                            val isInterested = iterator.next().receiveResults(queryResults)
                            if (!isInterested) {
                                iterator.remove()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    showErrorDialog("Unable to process the query.", project)
                }
                LOG.error(e)
            }
            SwingUtilities.invokeLater {
                contextHelperPanel.setQueryingStatus(false)
            }
        }
    }

    fun enterNewSession() {
        sessionID = "$installationID-${System.currentTimeMillis()}"
    }

    fun sendContextsMessage(caretOffset: Int, documentText: String) {
        usageCollector.sendContextsMessage(installationID, sessionID, caretOffset, documentText)
    }

    fun sendQuestionsMessage(request: String, questionIds: List<Long>) {
        usageCollector.sendQuestionsMessage(sessionID, request, questionIds)
    }

    fun sendClicksMessage(itemID: String) {
        usageCollector.sendClicksMessage(sessionID, itemID)
    }

    fun sendHelpfulMessage(itemID: String) {
        usageCollector.sendHelpfulMessage(sessionID, itemID)
    }

    companion object {
        private val LOG = Logger.getInstance(
            "ru.spb.se.contexthelper.component.ContextHelperProjectComponent")

        private const val localSeverHostName = "93.92.205.31"

        private val installationID = PermanentInstallationID.get()

        /** Last part of the name for {@link NamedComponent}. */
        private const val COMPONENT_NAME = "ContextHelperProjectComponent"
        private const val ICON_PATH_TOOL_WINDOW = "/icons/se-icon.png"

        private const val STACK_EXCHANGE_API_KEY = "F)x9bhGombhjqpnXt)5Mwg(("
        private val STACK_EXCHANGE_SITE = StackExchangeSite.STACK_OVERFLOW

        private const val GOOGLE_SEARCH_API_KEY = "AIzaSyBXQg39PaVjqONPEL4eubyA7S-pEuqVKOc"

        fun getFor(project: Project): ContextHelperProjectComponent =
            project.getComponent(ContextHelperProjectComponent::class.java)
    }
}