package ru.spb.se.contexthelper.ui

import com.google.code.stackexchange.schema.Answer
import com.google.code.stackexchange.schema.Comment
import com.google.code.stackexchange.schema.Question
import com.intellij.openapi.util.IconLoader
import java.awt.Component
import java.text.DecimalFormat
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.TreeCellRenderer

/** Renders StackExchange elements inside JTree. */
class StackExchangeThreadsTreeCellRender: TreeCellRenderer {
    override fun getTreeCellRendererComponent(
        tree: JTree?, value: Any?,
        selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean
    ): Component {
        val label = JLabel()
        when (value) {
            is Question -> {
                label.text = "<html>[${convertScoreToString(value.score)}] ${value.title}</html>"
                label.icon = IconLoader.getIcon(QUESTION_ICON_PATH)
            }
            is Answer -> {
                label.text = "<html>[${convertScoreToString(value.score)}] Answer </html>"
                label.icon = IconLoader.getIcon(
                    if (value.isAccepted) ACCEPTED_ANSWER_ICON_PATH else ANSWER_ICON_PATH)
            }
            is Comment -> {
                label.text = "<html>[${convertScoreToString(value.score)}] Comment </html>"
                label.icon = IconLoader.getIcon(COMMENT_ICON_PATH)
            }
        }
        return label
    }

    companion object {
        private const val QUESTION_ICON_PATH = "/icons/so-icon.png"
        private const val ACCEPTED_ANSWER_ICON_PATH = "/icons/answer-accepted.png"
        private const val ANSWER_ICON_PATH = "/icons/answer.png"
        private const val COMMENT_ICON_PATH = "/icons/comment.png"

        fun convertScoreToString(score: Long): String {
            val numberFormat = DecimalFormat("+#;-#")
            return numberFormat.format(score)
        }
    }
}