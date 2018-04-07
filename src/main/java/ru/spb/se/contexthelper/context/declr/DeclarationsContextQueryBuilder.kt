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
        val relevantTypes = contextTrie.getRelevantTypes(3)
        return Query(relevantTypes.map { Keyword(it.simpleName, 1) }.toList())
    }

    companion object {
        private val JAVA_PRIMITIVE_TYPE_NAMES =
            setOf("void", "boolean", "byte", "char", "short", "int", "long", "float", "double")
    }
}