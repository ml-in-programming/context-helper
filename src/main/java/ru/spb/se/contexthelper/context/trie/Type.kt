package ru.spb.se.contexthelper.context.trie

/** Represents the qualified name of an available [Class] in the context. */
data class Type(val parts: List<String>)