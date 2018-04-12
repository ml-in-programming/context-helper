package ru.spb.se.contexthelper.context.trie

/** Represents the qualified name of an available [Class] in the context. */
data class Type(val parts: List<String>) {
    constructor(qualifiedName: String) : this(qualifiedName.split(DOT_SYMBOL_REGEX))

    fun simpleName(): String = parts.last()

    fun fullName(): String = parts.joinToString(".")

    companion object {
        private val DOT_SYMBOL_REGEX = Regex("\\.")
    }
}