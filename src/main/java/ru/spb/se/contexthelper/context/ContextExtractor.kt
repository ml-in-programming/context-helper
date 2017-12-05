package ru.spb.se.contexthelper.context

import com.intellij.psi.*
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextQueryBuilder
import ru.spb.se.contexthelper.context.trie.Type

class ContextProcessor(private val psiElement: PsiElement) {
    fun generateQuery(): Query {
        generateQueryIfInPsiReferenceExpression()?.let { return it }
        return generateGenericQuery()
    }

    private fun generateQueryIfInPsiReferenceExpression(): Query? {
        val reference = findReferenceParent(psiElement) ?: return null
        val leftType = getLeftPartReferenceType(reference.firstChild) ?: return null
        val rightIdentifier = reference.children.find { it is PsiIdentifier } ?: return null
        val rightIdentifierParts = rightIdentifier.text.split(UPPERCASE_REGEX)
        val keywords = mutableListOf<Keyword>()
        keywords.add(Keyword(leftType.simpleName, rightIdentifierParts.size + 1))
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

    companion object {
        private val UPPERCASE_REGEX = Regex("(?=\\p{Upper})")
    }
}