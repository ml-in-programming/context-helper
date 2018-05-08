package ru.spb.se.contexthelper.context

import com.intellij.psi.*
import ru.spb.se.contexthelper.context.trie.Type

/** Exception is thrown if the query can not be built because of the lack of the context. */
class NotEnoughContextException : RuntimeException()

/** Class for representing the keywords extracted by ContextProcessors. */
class Keyword(val word: String)

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

fun composeQueryAroundElement(element: PsiElement?): Query {
    if (element == null) {
        return Query(emptyList())
    }
    when (element) {
        is PsiReferenceExpression -> {
            val leftType = getReferenceObjectType(element.firstChild)
            if (leftType == null) {
                val resolved = element.resolve()
                if (resolved != null) {
                    when (resolved) {
                        is PsiField -> {
                            getRelevantTypeName(resolved.parent)?.let {
                                return Query(listOf(
                                    Keyword(Type(it).simpleName()),
                                    Keyword(resolved.name)
                                ))
                            }
                        }
                        is PsiMethod -> {
                            getRelevantTypeName(resolved.parent)?.let {
                                val keywords = mutableListOf(Keyword(Type(it).simpleName()))
                                resolved.name.splitByUppercase().forEach {
                                    keywords.add(Keyword(it.toLowerCase()))
                                }
                                return Query(keywords.toList())
                            }
                        }
                        else -> {
                            getRelevantTypeName(resolved)?.let {
                                val type = Type(it)
                                return Query(listOf(Keyword(type.simpleName())))
                            }
                        }
                    }
                }
            } else {
                val keywords = mutableListOf<Keyword>()
                keywords.add(Keyword(leftType.simpleName()))
                val rightIdentifier = element.children.find { it is PsiIdentifier }
                rightIdentifier?.text?.splitByUppercase()?.map { it.toLowerCase() }?.forEach {
                    keywords.add(Keyword(it))
                }
                return Query(keywords)
            }
        }
        is PsiMethodCallExpression -> {
            val keywords = mutableListOf<Keyword>()
            val expression = element.methodExpression
            val leftType = getReferenceObjectType(expression.firstChild)
            if (leftType != null) {
                keywords.add(Keyword(leftType.simpleName()))
            }
            val rightIdentifier = expression.children.find { it is PsiIdentifier }
            rightIdentifier?.text?.splitByUppercase()?.map { it.toLowerCase() }?.forEach {
                keywords.add(Keyword(it))
            }
            return Query(keywords)
        }
        is PsiNewExpression -> {
            val createReference = element.classReference?.resolve()
            if (createReference != null) {
                val type = getRelevantTypeName(createReference)?.let { Type(it) }
                if (type != null) {
                    return Query(
                        listOf(Keyword("new"), Keyword(type.simpleName())))
                }
            }
        }
        is PsiTypeElement -> {
            element.innermostComponentReferenceElement?.let {
                val type = Type(it.qualifiedName)
                return Query(listOf(Keyword(type.simpleName())))
            }
        }
        is PsiLocalVariable -> {
            val type = getRelevantTypeName(element)?.let { Type(it) }
            if (type != null) {
                return Query(
                    listOf(Keyword(type.simpleName())))
            }
        }
    }
    return composeQueryAroundElement(element.parent)
}