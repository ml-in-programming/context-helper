package ru.spb.se.contexthelper.context

import com.intellij.psi.*
import ru.spb.se.contexthelper.context.trie.Type

/** Exception is thrown if the query can not be built because of the lack of the context. */
class NotEnoughContextException : RuntimeException()

class Keyword(val word: String, val weight: Int)

/** Represents the context by keywords. */
data class Query(val keywords: List<Keyword>)

private val UPPERCASE_REGEX = Regex("(?=\\p{Upper})")

fun String.splitByUppercase(): List<String> = split(UPPERCASE_REGEX)

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

fun getReferenceObjectType(element: PsiElement): Type? {
    if (element is PsiReferenceExpression) {
        val resolved = element.resolve()
        if (resolved != null) {
            getRelevantTypeName(resolved)?.let {
                return Type(it)
            }
        }
    } else if (element is PsiMethodCallExpression) {
        val resolvedMethod = element.resolveMethod()
        resolvedMethod?.returnType?.let {
            return Type(it.canonicalText)
        }
    } else if (element is PsiNewExpression) {
        element.classReference?.let {
            return Type(it.qualifiedName)
        }
    }
    return null
}