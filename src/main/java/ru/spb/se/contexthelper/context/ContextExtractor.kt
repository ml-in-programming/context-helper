package ru.spb.se.contexthelper.context

import com.intellij.psi.*
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextQueryBuilder
import ru.spb.se.contexthelper.context.trie.Type

class ContextProcessor(private val psiElement: PsiElement) {
    fun generateQuery(): String {
        generateQueryIfInPsiReferenceExpression()?.let { return it }
        return generateGenericQuery()
    }

    private fun generateQueryIfInPsiReferenceExpression(): String? {
        val parent = psiElement.parent
        if (parent is PsiReferenceExpression || parent is PsiMethodCallExpression) {
            val leftType = getLeftPartReferenceType(parent.firstChild)
            leftType?.let {
                return "How to get ${psiElement.text} from ${it.simpleName} in Java?"
            }
        }
        return null
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

    private fun generateGenericQuery(): String {
        val declarationsContextExtractor = DeclarationsContextExtractor(psiElement)
        val context = declarationsContextExtractor.context
        val queryBuilder = DeclarationsContextQueryBuilder(context)
        return queryBuilder.buildQuery()
    }
}