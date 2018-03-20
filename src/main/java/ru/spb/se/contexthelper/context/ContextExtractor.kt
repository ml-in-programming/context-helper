package ru.spb.se.contexthelper.context

import com.intellij.psi.*
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextQueryBuilder
import ru.spb.se.contexthelper.context.trie.Type

class ContextProcessor(initPsiElement: PsiElement) {
    private val psiElement =
        if (initPsiElement is PsiJavaToken) initPsiElement.prevSibling else initPsiElement

    fun generateQuery(): String {
        val nearCursorQuery = generateQueryAroundPsiElement()
        val genericQuery = generateGenericQuery()
        val questionFromGeneric = genericQuery.keywords.joinToString("|") { it.word }
        return if (nearCursorQuery != null) {
            nearCursorQuery.keywords.joinToString(" ") { it.word } +
                " (\"\"|$questionFromGeneric) java"
        } else {
            "($questionFromGeneric) java"
        }
    }

    private fun generateQueryAroundPsiElement(): Query? {
        val keywords = mutableListOf<Keyword>()
        if (psiElement is PsiNewExpression) {
            val createReference = psiElement.classReference?.resolve() ?: return null
            val type = getRelevantTypeName(createReference)?.let { Type(it) }
            if (type != null) {
                keywords.add(Keyword("new", 1))
                keywords.add(Keyword(type.parts.joinToString("."), 1))
                return Query(keywords)
            }
        }
        val reference = findReferenceParent(psiElement) ?: return null
        val leftType = getLeftPartReferenceType(reference.firstChild)
        val rightIdentifier = reference.children.find { it is PsiIdentifier } ?: return null
        val rightIdentifierParts = rightIdentifier.text.splitByUppercase()
        if (leftType != null) {
            keywords.add(Keyword(leftType.parts.joinToString("."), 1))
        }
        rightIdentifierParts.forEach { keywords.add(Keyword(it, 1)) }
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

    private fun generateGenericQuery(): Query {
        val declarationsContextExtractor = DeclarationsContextExtractor(psiElement)
        val context = declarationsContextExtractor.context
        val queryBuilder = DeclarationsContextQueryBuilder(context)
        return queryBuilder.buildQuery()
    }
}