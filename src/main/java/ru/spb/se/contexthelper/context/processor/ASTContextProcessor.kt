package ru.spb.se.contexthelper.context.processor

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiMethod
import ru.spb.se.contexthelper.context.Keyword
import ru.spb.se.contexthelper.context.getRelevantTypeName
import ru.spb.se.contexthelper.context.trie.Type

class ASTContextProcessor(initPsiElement: PsiElement) : TextQueryContextProcessor(initPsiElement) {
    override fun generateTextQuery(): String {
        val references = mutableListOf<PsiJavaCodeReferenceElement>()
        findCodeReferencesUp(psiElement, Int.MAX_VALUE, references)
        val nearestReferences = references.mapNotNull { reference ->
            reference.resolve()?.toKeyword()?.let { keyword ->
                reference.textOffset to keyword
            }
        }.sortedBy {
            Math.abs(psiElement.textOffset - it.first)
        }.take(PREFIX_KEYWORDS + CONTEXT_KEYWORDS)
        val queryBuilder = mutableListOf<String>()
        if (nearestReferences.isEmpty()) {
            queryBuilder.add(psiElement.text)
        } else {
            val orderedPrefixKeywords =
                nearestReferences.take(PREFIX_KEYWORDS)
                    .sortedBy { it.first }
                    .map { it.second }
            val prefixOptions =
                (0 until orderedPrefixKeywords.size).map { prefixLength ->
                    orderedPrefixKeywords.take(prefixLength + 1).joinToString(" ") { it.word }
                }.reversed()
            queryBuilder.add(
                prefixOptions.joinToString("|", "(", ")") { "\"$it\"" })
            val contextOptions = mutableListOf<String>()
            nearestReferences.drop(PREFIX_KEYWORDS).forEach { contextOptions.add(it.second.word) }
            contextOptions.add("")
            queryBuilder.add(contextOptions.joinToString("|", "(", ")") { "\"$it\"" })
        }
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
        private const val PREFIX_KEYWORDS = 1
        private const val CONTEXT_KEYWORDS = 4

        private fun PsiElement.toKeyword(): Keyword? =
            if (this is PsiMethod) {
                Keyword(name)
            } else {
                getRelevantTypeName(this)?.let {
                    val type = Type(it)
                    Keyword(type.simpleName())
                }
            }
    }
}