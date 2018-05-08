package ru.spb.se.contexthelper.context.processor

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import ru.spb.se.contexthelper.context.composeQueryAroundElement
import ru.spb.se.contexthelper.context.getRelevantTypeName
import ru.spb.se.contexthelper.context.trie.Type

class ASTContextProcessor(initPsiElement: PsiElement) : TextQueryContextProcessor(initPsiElement) {
    override fun generateTextQuery(): String {
        val references = mutableListOf<PsiJavaCodeReferenceElement>()
        findCodeReferencesUp(psiElement, Int.MAX_VALUE, references)
        val nearestReferences = references.sortedBy {
            Math.abs(psiElement.textOffset - it.textOffset)
        }.mapNotNull { reference ->
            reference.resolve()?.let {
                getRelevantTypeName(it)?.let {
                    val type = Type(it)
                    type.simpleName()
                }
            }
        }.take(CONTEXT_KEYWORDS)
        val queryBuilder = mutableListOf<String>()
        val contextOptions = mutableListOf<String>()
        val nearCursorQuery = composeQueryAroundElement(psiElement)
        if (nearCursorQuery.keywords.isEmpty()) {
            if (nearestReferences.isEmpty()) {
                // Following the naive approach.
                queryBuilder.add(psiElement.text)
            } else {
                queryBuilder.add(nearestReferences.first())
                contextOptions.addAll(nearestReferences.drop(1))
            }
        } else {
            queryBuilder.add(nearCursorQuery.keywords.joinToString(" ") { it.word })
            contextOptions.addAll(nearestReferences)
        }
        contextOptions.add("")
        queryBuilder.add(contextOptions.joinToString("|", "(", ")") { "\"$it\"" })
        queryBuilder.add("java")
        return queryBuilder.joinToString(" ")
    }

    private fun findCodeReferencesUp(
        psiElement: PsiElement,
        referencesToFind: Int,
        references: MutableList<PsiJavaCodeReferenceElement>
    ): Int {
        assert(referencesToFind > 0)
        var referencesLeftToFind = referencesToFind
        if (psiElement is PsiJavaCodeReferenceElement) {
            references.add(psiElement)
            referencesLeftToFind -= 1
        }
        val parent = psiElement.parent
        if (parent != null && parent !is PsiDirectory) {
            val childIterator = parent.children.iterator()
            while (childIterator.hasNext() && referencesLeftToFind > 0) {
                val child = childIterator.next()
                if (child !== psiElement) {
                    referencesLeftToFind =
                        findCodeReferencesDown(child, referencesLeftToFind, references)
                }
            }
            if (referencesLeftToFind > 0) {
                referencesLeftToFind =
                    findCodeReferencesUp(parent, referencesLeftToFind, references)
            }
        }
        return referencesLeftToFind
    }

    private fun findCodeReferencesDown(
        psiElement: PsiElement,
        referencesToFind: Int,
        references: MutableList<PsiJavaCodeReferenceElement>
    ): Int {
        assert(referencesToFind > 0)
        var referencesLeftToFind = referencesToFind
        if (psiElement is PsiJavaCodeReferenceElement) {
            references.add(psiElement)
            referencesLeftToFind -= 1
        }
        val childIterator = psiElement.children.iterator()
        while (childIterator.hasNext() && referencesLeftToFind > 0) {
            val child = childIterator.next()
            referencesLeftToFind = findCodeReferencesDown(child, referencesLeftToFind, references)
        }
        return referencesLeftToFind
    }

    companion object {
        private const val CONTEXT_KEYWORDS = 4
    }
}