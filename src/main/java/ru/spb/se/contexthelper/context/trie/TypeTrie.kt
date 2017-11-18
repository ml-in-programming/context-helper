package ru.spb.se.contexthelper.context.trie

/** Prefix tree built from available [Type]s. */
data class TypeTrie(val root: Node = Node()) {
    fun addType(type: Type) {
        var node = root
        for (part in type.parts) {
            node.typesInSubtree++
            node = node.edgeMap.getOrPut(part, { Node() })
        }
        node.typesInSubtree++
        node.typesInNode++
    }

    data class Node(
        var typesInNode: Int = 0,
        var typesInSubtree: Int = 0,
        val edgeMap: HashMap<String, Node> = hashMapOf()
    ) {
        fun printNode(indent: String = "") {
            println("$typesInSubtree")
            for (entry in edgeMap) {
                print("$indent ${entry.key} ")
                entry.value.printNode(indent + "  ")
            }
        }
    }
}