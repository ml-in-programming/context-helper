package ru.spb.se.contexthelper.context.declr

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPackage
import com.intellij.psi.PsiVariable
import ru.spb.se.contexthelper.context.trie.Type
import ru.spb.se.contexthelper.context.trie.TypeContextTrie
import java.util.stream.Collectors

class DeclarationsContextQueryBuilder(private val declarationsContext: DeclarationsContext) {
    fun buildQuery(): String {
        val contextTrie = TypeContextTrie()
        declarationsContext.declarations
            .forEach {
                val psiElement = it.psiElement
                val relevantTypeName =
                    when (psiElement) {
                        is PsiVariable -> {
                            // PsiLocalVariable || PsiField || PsiParameter
                            val psiTypeElement = psiElement.typeElement
                            psiTypeElement?.innermostComponentReferenceElement?.qualifiedName
                        }
                        is PsiMethod -> {
                            // Currently omitting PsiMethod for the query building.
                            null
                        }
                        is PsiClass -> {
                            psiElement.qualifiedName
                        }
                        is PsiPackage -> {
                            // Currently omitting PsiPackage for the query building.
                            null
                        }
                        else -> {
                            // TODO(niksaz): Use intellij Logger.
                            System.err.println("Unexpected PsiElement is used for declarations: " + it)
                            null
                        }
                    }
                if (relevantTypeName != null) {
                    val type = Type(relevantTypeName.split(DOT_SYMBOL_REGEX))
                    contextTrie.addType(type, it.declarationHolderLevel)
                }
            }
        val relevantParts = contextTrie.buildRelevantParts()
        return relevantParts.stream()
            .flatMap { it.split(UPPERCASE_LETTER_REGEX).stream() }
            .limit(MAX_PARTS_FOR_QUERY)
            .collect(Collectors.joining(" "))
    }

    companion object {
        private val MAX_PARTS_FOR_QUERY = 32L

        private val DOT_SYMBOL_REGEX = Regex("\\.")
        private val UPPERCASE_LETTER_REGEX = Regex("(?=\\p{Upper})")
    }
}