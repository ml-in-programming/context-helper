package ru.spb.se.contexthelper.context.processor

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import ru.spb.se.contexthelper.context.composeQueryAroundElement
import ru.spb.se.contexthelper.context.getRelevantTypeName
import ru.spb.se.contexthelper.context.trie.Type

class ASTContextProcessor(initPsiElement: PsiElement) : TextQueryContextProcessor(initPsiElement) {
    override fun generateTextQuery(): String {
        printTreeUp(psiElement, 10)
        val references = mutableListOf<PsiJavaCodeReferenceElement>()
        println("Found references: $references")
        findCodeReferencesUp(psiElement, 4, references)
        val referenceTypeNames = references.mapNotNull { reference ->
            reference.resolve()?.let {
                getRelevantTypeName(it)?.let {
                    val type = Type(it)
                    type.simpleName()
                }
            }
        }
        val queryBuilder = mutableListOf<String>()
        val contextOptions = mutableListOf<String>()
        val nearCursorQuery = composeQueryAroundElement(psiElement)
        if (nearCursorQuery.keywords.isEmpty()) {
            if (referenceTypeNames.isEmpty()) {
                // Following the naive approach.
                queryBuilder.add(psiElement.text)
            } else {
                queryBuilder.add(referenceTypeNames.first())
                contextOptions.addAll(referenceTypeNames.drop(1))
            }
        } else {
            queryBuilder.add(nearCursorQuery.keywords.joinToString(" ") { it.word })
            contextOptions.addAll(referenceTypeNames)
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
            var index = parent.children.indexOfFirst { it === psiElement }
            if (index != -1) {
                index -= 1
                while (index >= 0 && referencesLeftToFind > 0) {
                    referencesLeftToFind =
                        findCodeReferencesDown(
                            parent.children[index], referencesLeftToFind, references)
                    index -= 1
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
        val childIterator = psiElement.children.reversed().iterator()
        while (childIterator.hasNext() && referencesLeftToFind > 0) {
            val child = childIterator.next()
            referencesLeftToFind = findCodeReferencesDown(child, referencesLeftToFind, references)
        }
        return referencesLeftToFind
    }

    private fun printTreeUp(psiElement: PsiElement, climb: Int): Int {
        val parent = psiElement.parent
        return if (climb == 0 || parent == null || parent is PsiDirectory) {
            println(psiElement)
            0
        } else {
            val indent = printTreeUp(parent, climb - 1)
            for (child in parent.children) {
                print(" ".repeat(2 * indent))
                print(child)
                if (child === psiElement) {
                    print(" <----")
                }
                if (child === this.psiElement) {
                    print(" [this]")
                }
                println()
            }
            indent
        } + 1
    }
}