package ru.spb.se.contexthelper.context.trie

/** Prefix tree built from available [Type]s. */
class TypeTrie {
    private val root = Node()

    fun addType(type: Type) {
        var node = root
        for (part in type.parts) {
            node = node.edgeMap.getOrPut(part, { Node() })
        }
        node.numberOfTypes++
    }

    fun printTrie() {
        root.printNode("")
    }

    data class Node(var numberOfTypes: Int = 0, val edgeMap: HashMap<String, Node> = hashMapOf()) {
        fun printNode(indent: String) {
            println("$numberOfTypes")
            for (entry in edgeMap) {
                print("$indent ${entry.key} ")
                entry.value.printNode(indent + "  ")
            }
        }
    }
}