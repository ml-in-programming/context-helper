package ru.spb.se.contexthelper.context

import com.intellij.psi.*
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextQueryBuilder
import ru.spb.se.contexthelper.context.trie.Type

class ContextProcessor(initPsiElement: PsiElement) {
    private val psiElement =
        if (initPsiElement is PsiJavaToken && initPsiElement.prevSibling != null)
            initPsiElement.prevSibling
        else
            initPsiElement

    fun generateQuery(): Query {
        val keywords = ArrayList<Keyword>()
        val nearCursorQuery = composeQueryAroundPsiElement()
        keywords.addAll(nearCursorQuery.keywords)
        val genericQuery = composeGenericQuery()
        keywords.addAll(genericQuery.keywords)
        if (keywords.isEmpty()) {
            throw NotEnoughContextException()
        }
        return Query(keywords)
    }

    private fun composeQueryAroundPsiElement(): Query {
        val keywords = mutableListOf<Keyword>()
        if (psiElement is PsiNewExpression) {
            val createReference = psiElement.classReference?.resolve() ?: return Query(emptyList())
            val type = getRelevantTypeName(createReference)?.let { Type(it) }
            if (type != null) {
                keywords.add(Keyword(type.simpleName, 1))
                return Query(keywords)
            }
        }
        val reference = findReferenceParent(psiElement) ?: return Query(emptyList())
        val leftType = getLeftPartReferenceType(reference.firstChild)
        if (leftType != null) {
            keywords.add(Keyword(leftType.simpleName, 1))
        }
        val rightIdentifier = reference.children.find { it is PsiIdentifier }
        if (rightIdentifier != null) {
            keywords.add(Keyword(rightIdentifier.text, 1))
        }
        return Query(keywords)
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

    private fun composeGenericQuery(): Query {
        val declarationsContextExtractor = DeclarationsContextExtractor(psiElement)
        val context = declarationsContextExtractor.context
        val queryBuilder = DeclarationsContextQueryBuilder(context)
        return queryBuilder.buildQuery()
    }
}