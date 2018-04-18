package ru.spb.se.contexthelper.context.processor

import com.intellij.psi.*
import ru.spb.se.contexthelper.context.*
import ru.spb.se.contexthelper.context.declr.DeclarationsContextExtractor
import ru.spb.se.contexthelper.context.declr.DeclarationsContextTypesExtractor
import ru.spb.se.contexthelper.context.trie.Type

class GCSContextProcessor(initPsiElement: PsiElement) : AbstractContextProcessor(initPsiElement) {
    fun generateQuery(): String {
        val queryBuilder = ArrayList<String>()
        var nearCursorQuery = composeQueryAroundElement(psiElement)
        if (nearCursorQuery.keywords.isEmpty()) {
            val text = psiElement.text
            if (text.chars().allMatch(Character::isLetterOrDigit)) {
                nearCursorQuery = Query(listOf(Keyword(text, 1)))
            }
        }
        if (nearCursorQuery.keywords.isNotEmpty()) {
            queryBuilder.add(nearCursorQuery.keywords.joinToString(" ") { it.word })
        }
        val genericQuery = composeGenericQuery()
        if (genericQuery != null) {
            val contextTerms = genericQuery.keywords.map { it.word }.toMutableList()
            if (queryBuilder.isNotEmpty()) {
                // Meaning there is something more important that we want to ask. So we allow Google
                // Search to omit the context.
                contextTerms.add("")
            }
            queryBuilder.add(contextTerms.joinToString("|", "(", ")") { "\"$it\""})
        }
        if (queryBuilder.isEmpty()) {
            throw NotEnoughContextException()
        }
        queryBuilder.add("java")
        return queryBuilder.joinToString(" ")
    }

    private fun composeQueryAroundElement(element: PsiElement?): Query {
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
                                        Keyword(Type(it).fullName(), 1),
                                        Keyword(resolved.name, 1)
                                    ))
                                }
                            }
                            is PsiMethod -> {
                                getRelevantTypeName(resolved.parent)?.let {
                                    val keywords = mutableListOf(Keyword(Type(it).fullName(), 1))
                                    resolved.name.splitByUppercase().forEach {
                                        keywords.add(Keyword(it.toLowerCase(), 1))
                                    }
                                    return Query(keywords.toList())
                                }
                            }
                            else -> {
                                getRelevantTypeName(resolved)?.let {
                                    val type = Type(it)
                                    return Query(listOf(Keyword(type.fullName(), 1)))
                                }
                            }
                        }
                    }
                } else {
                    val keywords = mutableListOf<Keyword>()
                    keywords.add(Keyword(leftType.fullName(), 1))
                    val rightIdentifier = element.children.find { it is PsiIdentifier }
                    rightIdentifier?.text?.splitByUppercase()?.map { it.toLowerCase() }?.forEach {
                        keywords.add(Keyword(it, 1))
                    }
                    return Query(keywords)
                }
            }
            is PsiMethodCallExpression -> {
                val keywords = mutableListOf<Keyword>()
                val expression = element.methodExpression
                val leftType = getReferenceObjectType(expression.firstChild)
                if (leftType != null) {
                    keywords.add(Keyword(leftType.fullName(), 1))
                }
                val rightIdentifier = expression.children.find { it is PsiIdentifier }
                rightIdentifier?.text?.splitByUppercase()?.map { it.toLowerCase() }?.forEach {
                    keywords.add(Keyword(it, 1))
                }
                return Query(keywords)
            }
            is PsiNewExpression -> {
                val createReference = element.classReference?.resolve()
                if (createReference != null) {
                    val type = getRelevantTypeName(createReference)?.let { Type(it) }
                    if (type != null) {
                        return Query(
                            listOf(Keyword("new", 1), Keyword(type.fullName(), 1)))
                    }
                }
            }
            is PsiTypeElement -> {
                element.innermostComponentReferenceElement?.let {
                    val type = Type(it.qualifiedName)
                    return Query(listOf(Keyword(type.fullName(), 1)))
                }
            }
        }
        return composeQueryAroundElement(element.parent)
    }

    private fun composeGenericQuery(): Query? {
        val declarationsContextExtractor = DeclarationsContextExtractor(psiElement)
        val context = declarationsContextExtractor.context
        val typesExtractor = DeclarationsContextTypesExtractor(context)
        val relevantTypes = typesExtractor.getRelevantTypes(2)
        val keywords = relevantTypes.map { Keyword(it.fullName(), 1) }
        return if (keywords.isEmpty()) null else Query(keywords)
    }
}