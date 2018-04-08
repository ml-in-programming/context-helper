package ru.spb.se.contexthelper.context

import com.intellij.psi.*
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextTypesExtractor
import ru.spb.se.contexthelper.context.trie.Type

class GCSContextProcessor(initPsiElement: PsiElement) {
    private val psiElement =
        if (initPsiElement is PsiJavaToken && initPsiElement.prevSibling != null)
            initPsiElement.prevSibling
        else
            initPsiElement

    fun generateQuery(): String {
        val queryBuilder = ArrayList<String>()
        val nearCursorQuery = composeQueryAroundPsiElement()
        if (nearCursorQuery != null) {
            queryBuilder.add(nearCursorQuery.keywords.joinToString(" ") { it.word })
        }
        val genericQuery = composeGenericQuery()
        if (genericQuery != null) {
            val questionFromGeneric = genericQuery.keywords.joinToString("|") { it.word }
            if (queryBuilder.isNotEmpty()) {
                queryBuilder.add("(\"\"|$questionFromGeneric)")
            } else {
                queryBuilder.add("($questionFromGeneric)")
            }
        }
        if (queryBuilder.isEmpty()) {
            throw NotEnoughContextException()
        }
        queryBuilder.add("java")
        return queryBuilder.joinToString(" ")
    }

    private fun composeQueryAroundPsiElement(): Query? {
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

    private fun composeGenericQuery(): Query? {
        val declarationsContextExtractor = DeclarationsContextExtractor(psiElement)
        val context = declarationsContextExtractor.context
        val typesExtractor = DeclarationsContextTypesExtractor(context)
        val relevantTypes = typesExtractor.getRelevantTypes(2)
        val keywords = relevantTypes.map { Keyword(it.parts.joinToString("."), 1) }
        return if (keywords.isEmpty()) null else Query(keywords)
    }
}