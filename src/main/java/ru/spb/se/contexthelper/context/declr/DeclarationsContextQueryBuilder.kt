package ru.spb.se.contexthelper.context.declr

import ru.spb.se.contexthelper.context.NotEnoughContextException
import ru.spb.se.contexthelper.context.getRelevantTypeName
import ru.spb.se.contexthelper.context.trie.Type
import ru.spb.se.contexthelper.context.trie.TypeContextTrie

class DeclarationsContextQueryBuilder(private val declarationsContext: DeclarationsContext) {
    fun buildQuery(): String {
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
            relevantTypes.size == 1 -> "How to use ${relevantTypes[0].simpleName} in Java?"
            else -> {
                val firstType = relevantTypes[0].simpleName
                val secondType = relevantTypes[1].simpleName
                "How to use $firstType with $secondType in Java?"
            }
        }
    }

    companion object {
        private val JAVA_PRIMITIVE_TYPE_NAMES =
            setOf("void", "boolean", "byte", "char", "short", "int", "long", "float", "double")
    }
}