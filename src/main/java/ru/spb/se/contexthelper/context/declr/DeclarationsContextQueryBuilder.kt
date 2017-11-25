package ru.spb.se.contexthelper.context.declr

import ru.spb.se.contexthelper.context.getRelevantTypeName
import ru.spb.se.contexthelper.context.trie.Type
import ru.spb.se.contexthelper.context.trie.TypeContextTrie
import java.util.stream.Collectors

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
        val relevantParts = contextTrie.buildRelevantParts()
        return relevantParts.stream()
            .flatMap { it.split(UPPERCASE_LETTER_REGEX).stream() }
            .limit(MAX_PARTS_FOR_QUERY)
            .collect(Collectors.joining(" "))
    }

    companion object {
        private val MAX_PARTS_FOR_QUERY = 32L

        private val UPPERCASE_LETTER_REGEX = Regex("(?=\\p{Upper})")

        private val JAVA_PRIMITIVE_TYPE_NAMES =
            setOf("boolean", "byte", "char", "short", "int", "long", "float", "double")
    }
}