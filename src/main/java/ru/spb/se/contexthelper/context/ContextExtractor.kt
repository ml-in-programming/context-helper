package ru.spb.se.contexthelper.context

import com.intellij.psi.*
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextQueryBuilder
import ru.spb.se.contexthelper.context.trie.Type

class ContextProcessor(initPsiElement: PsiElement) {
    private val psiElement =
        if (initPsiElement is PsiJavaToken) initPsiElement.prevSibling else initPsiElement

    fun generateQuery(): String {
        val referenceQuery = generateQueryIfInPsiReferenceExpression()
        val genericQuery = generateGenericQuery()
        val questionFromGeneric = genericQuery.keywords.joinToString("|") { it.word }
        return if (referenceQuery != null) {
            referenceQuery.keywords.joinToString(" ") { it.word } +
                " (\"\"|$questionFromGeneric) java"
        } else {
            "($questionFromGeneric) java"
        }
    }

    private fun generateQueryIfInPsiReferenceExpression(): Query? {
        val reference = findReferenceParent(psiElement) ?: return null
        val leftType = getLeftPartReferenceType(reference.firstChild) ?: return null
        val rightIdentifier = reference.children.find { it is PsiIdentifier } ?: return null
        val rightIdentifierParts = rightIdentifier.text.splitByUppercase()
        val keywords = mutableListOf<Keyword>()
        keywords.add(Keyword(leftType.parts.joinToString("."), 1))
        rightIdentifierParts.forEach {
            keywords.add(Keyword(it, 1))
        }
        return Query(
            keywords,
            "How to ${rightIdentifierParts.joinToString(" ")} ${leftType.simpleName}")
    }

    private fun findReferenceParent(psiElement: PsiElement?): PsiElement? {
        return when (psiElement) {
            null -> null
            is PsiReferenceExpression -> psiElement
            is PsiMethodCallExpression -> psiElement.methodExpression
            else -> findReferenceParent(psiElement.parent)
        }
    }

    private fun getLeftPartReferenceType(element: PsiElement): Type? {
        if (element is PsiReferenceExpression) {
            val resolvedFirstChild = element.resolve()
            if (resolvedFirstChild != null) {
                getRelevantTypeName(resolvedFirstChild)?.let {
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

    private fun generateGenericQuery(): Query {
        val declarationsContextExtractor = DeclarationsContextExtractor(psiElement)
        val context = declarationsContextExtractor.context
        val queryBuilder = DeclarationsContextQueryBuilder(context)
        return queryBuilder.buildQuery()
    }
}