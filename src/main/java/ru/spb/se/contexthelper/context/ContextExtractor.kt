package ru.spb.se.contexthelper.context

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceExpression
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextQueryBuilder
import ru.spb.se.contexthelper.context.trie.Type

class ContextProcessor(private val psiElement: PsiElement) {
    fun generateQuery(): String {
        generateQueryIfInPsiReferenceExpression()?.let { return it }
        return generateGenericQuery()
    }

    private fun generateQueryIfInPsiReferenceExpression(): String? {
        val parent = psiElement.parent
        if (parent is PsiReferenceExpression) {
            val firstChild = parent.firstChild
            if (firstChild is PsiReferenceExpression) {
                val resolvedFirstChild = firstChild.resolve()
                if (resolvedFirstChild != null) {
                    val typeName = getRelevantTypeName(resolvedFirstChild)
                    if (typeName != null) {
                        val type = Type(typeName)
                        return "How to get ${psiElement.text} from ${type.simpleName} in Java?"
                    }
                }
            }
        }
        return null
    }

    private fun generateGenericQuery(): String {
        val declarationsContextExtractor = DeclarationsContextExtractor(psiElement)
        val context = declarationsContextExtractor.context
        val queryBuilder = DeclarationsContextQueryBuilder(context)
        val query = queryBuilder.buildQuery()
        return "$query java"
    }
}