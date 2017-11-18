package ru.spb.se.contexthelper.context.declr

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPackage
import com.intellij.psi.PsiVariable
import ru.spb.se.contexthelper.context.NotEnoughContextException
import ru.spb.se.contexthelper.context.trie.Type
import ru.spb.se.contexthelper.context.trie.TypeTrie
import java.util.stream.Collectors

class DeclarationsContextQueryBuilder(private val declarationsContext: DeclarationsContext) {
    fun buildQuery(): String {
        val declaredQualifiedClassNames =
            declarationsContext.declarations
                .mapNotNull {
                    val psiElement = it.psiElement
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
                }
        if (declaredQualifiedClassNames.isEmpty()) {
            throw NotEnoughContextException("No class is declared in the current context.")
        }
        val trie = TypeTrie()
        declaredQualifiedClassNames
            .map { Type(it.split(DOT_SYMBOL_REGEX)) }
            .forEach { trie.addType(it) }
        trie.root.printNode()
        return declaredQualifiedClassNames.stream()
            .limit(MAX_CLASSES_FOR_QUERY.toLong())
            .flatMap { it.split(DOT_SYMBOL_REGEX).stream() }
            .flatMap { it.split(UPPERCASE_LETTER_REGEX).stream() }
            .collect(Collectors.joining(" "))
    }

    companion object {
        private val MAX_CLASSES_FOR_QUERY = 2

        private val DOT_SYMBOL_REGEX = Regex("\\.")
        private val UPPERCASE_LETTER_REGEX = Regex("(?=\\p{Upper})")
    }
}