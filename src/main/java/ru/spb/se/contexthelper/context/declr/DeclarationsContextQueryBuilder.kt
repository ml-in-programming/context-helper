package ru.spb.se.contexthelper.context.declr

import ru.spb.se.contexthelper.context.*
import ru.spb.se.contexthelper.context.trie.Type
import ru.spb.se.contexthelper.context.trie.TypeContextTrie

class DeclarationsContextQueryBuilder(private val declarationsContext: DeclarationsContext) {
    fun buildQuery(): Query {
        val contextTrie = TypeContextTrie()
        declarationsContext.declarations
            .forEach {
                val psiElement = it.psiElement
                val relevantTypeName = getRelevantTypeName(psiElement)
                if (relevantTypeName != null && relevantTypeName !in JAVA_PRIMITIVE_TYPE_NAMES) {
                    val type = Type(relevantTypeName)
                    contextTrie.addType(type, it.parentLevel)
                }
            }
        val relevantTypes = contextTrie.getRelevantTypes(2)
        return when {
            relevantTypes.isEmpty() -> throw NotEnoughContextException()
            relevantTypes.size == 1 -> {
                val typeName = relevantTypes[0].simpleName
                val keywords = typeName.splitByUppercase().map { Keyword(it, 1) }
                Query(keywords, "How to use $typeName")
            }
            else -> {
                val firstTypeName = relevantTypes[0].simpleName
                val secondTypeName = relevantTypes[1].simpleName
                val keywords =
                    listOf(firstTypeName, secondTypeName)
                        .flatMap { it.splitByUppercase() }
                        .map { Keyword(it, 1) }
                Query(keywords, "How to use $firstTypeName with $secondTypeName")
            }
        }
    }

    companion object {
        private val JAVA_PRIMITIVE_TYPE_NAMES =
            setOf("void", "boolean", "byte", "char", "short", "int", "long", "float", "double")
    }
}