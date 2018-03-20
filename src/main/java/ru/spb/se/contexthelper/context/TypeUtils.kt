package ru.spb.se.contexthelper.context

import com.intellij.psi.*

/** Exception is thrown if the query can not be built because of the lack of the context. */
class NotEnoughContextException : RuntimeException()

class Keyword(word: String, val weight: Int) {
    val word = word.toLowerCase()
}

/** Represents the context by keywords. */
data class Query(val keywords: List<Keyword>)

fun getRelevantTypeName(psiElement: PsiElement): String? =
    when (psiElement) {
        is PsiVariable -> {
            // PsiLocalVariable || PsiField || PsiParameter
            val psiTypeElement = psiElement.typeElement
            val result = psiTypeElement?.innermostComponentReferenceElement?.qualifiedName
            result ?: psiElement.type.canonicalText
        }
        is PsiMethod -> {
            val psiReturnTypeElement = psiElement.returnType
            psiReturnTypeElement?.canonicalText
        }
        is PsiClass -> {
            psiElement.qualifiedName
        }
        else -> {
            null
        }
    }

private val UPPERCASE_REGEX = Regex("(?=\\p{Upper})")

fun String.splitByUppercase(): List<String> = split(UPPERCASE_REGEX)