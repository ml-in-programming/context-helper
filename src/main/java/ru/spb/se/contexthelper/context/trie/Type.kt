package ru.spb.se.contexthelper.context.trie

/** Represents the qualified name of an available [Class] in the context. */
data class Type(val parts: List<String>) {
    val simpleName = parts.last()

    constructor(qualifiedName: String) : this(qualifiedName.split(DOT_SYMBOL_REGEX))

    companion object {
        private val DOT_SYMBOL_REGEX = Regex("\\.")
    }
}