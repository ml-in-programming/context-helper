package ru.spb.se.contexthelper.context.processor

import com.intellij.psi.PsiElement
import ru.spb.se.contexthelper.context.Keyword
import ru.spb.se.contexthelper.context.NotEnoughContextException
import ru.spb.se.contexthelper.context.Query
import ru.spb.se.contexthelper.context.composeQueryAroundElement
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextTypesExtractor

class DeclarationsContextProcessor(
    initPsiElement: PsiElement
) : TextQueryContextProcessor(initPsiElement) {

    override fun generateTextQuery(): String {
        val queryBuilder = ArrayList<String>()
        val nearCursorQuery = composeQueryAroundElement(psiElement)
        if (nearCursorQuery.keywords.isEmpty()) {
            // Following the naive approach.
            queryBuilder.add(psiElement.text)
        } else {
            queryBuilder.add(nearCursorQuery.keywords.joinToString(" ") { it.word })
        }
        val genericQuery = composeGenericQuery()
        if (genericQuery != null) {
            val contextTerms = genericQuery.keywords.map { it.word }.toMutableList()
            if (queryBuilder.isNotEmpty()) {
                // Meaning there is something more important that we want to ask. So we allow Google
                // Search to omit the context.
                contextTerms.add("")
            }
            queryBuilder.add(contextTerms.joinToString("|", "(", ")") { "\"$it\""})
        }
        if (queryBuilder.isEmpty()) {
            throw NotEnoughContextException()
        }
        queryBuilder.add("java")
        return queryBuilder.joinToString(" ")
    }

    private fun composeGenericQuery(): Query? {
        val declarationsContextExtractor = DeclarationsContextExtractor(psiElement)
        val context = declarationsContextExtractor.context
        val typesExtractor = DeclarationsContextTypesExtractor(context)
        val relevantTypes = typesExtractor.getRelevantTypes(2)
        val keywords = relevantTypes.map { Keyword(it.simpleName()) }
        return if (keywords.isEmpty()) null else Query(keywords)
    }
}