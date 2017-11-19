package ru.spb.se.contexthelper.context.trie

/** Prefix tree built from available [Type]s. */
data class TypeContextTrie(private val root: Node = Node()) {
    fun addType(type: Type, typeLevel: Int) {
        root.addType(type.parts, 0, typeLevel)
    }

    fun buildRelevantParts(): List<String> {
        val relevantParts = mutableListOf<String>()
        root.toEvaluatedNode().findRelevantParts(TYPES_TO_CONSIDER, relevantParts)
        return relevantParts
    }

    fun printTrie() {
        root.toEvaluatedNode().printNode()
    }


    data class Node(
        private var mostRelevantLevel: Int? = null,
        private val edgeMap: HashMap<String, Node> = hashMapOf()
    ) {
        fun addType(parts: List<String>, partIndex: Int, typeLevel: Int) {
            if (parts.size == partIndex) {
                if (mostRelevantLevel == null || mostRelevantLevel!! > typeLevel) {
                    mostRelevantLevel = typeLevel
                }
                return
            }
            val node = edgeMap.getOrPut(parts[partIndex], { Node() })
            node.addType(parts, partIndex + 1, typeLevel)
        }

        fun toEvaluatedNode(): EvaluatedNode {
            val levels = mutableListOf<Pair<String?, Int>>()
            val evaluatedEdgeMap = mutableMapOf<String, EvaluatedNode>()
            if (mostRelevantLevel != null) {
                levels.add(null to mostRelevantLevel!!)
            }
            for (entry in edgeMap) {
                val childEvaluatedNode = entry.value.toEvaluatedNode()
                evaluatedEdgeMap.put(entry.key, childEvaluatedNode)
                childEvaluatedNode.subtreeLevels.mapTo(levels) { entry.key  to it.second }
            }
            levels.sortBy { it.second }
            return EvaluatedNode(levels.toList(), evaluatedEdgeMap)
        }
    }

    data class EvaluatedNode(
        val subtreeLevels: List<Pair<String?, Int>>,
        private val edgeMap: Map<String, EvaluatedNode>
    ) {
        fun findRelevantParts(typesToFind: Long, parts: MutableList<String>) {
            val typesToFindMap = mutableMapOf<String?, Long>()
            subtreeLevels.stream()
                .limit(typesToFind)
                .forEach { typesToFindMap.merge(it.first, 1, Long::plus) }
            for (subtreeLevel in subtreeLevels) {
                val part = subtreeLevel.first
                val typesInChild = typesToFindMap[part] ?: continue
                typesToFindMap.remove(part)
                if (part == null) {
                    // It is current vertex and we have already added name components.
                } else {
                    parts.add(part)
                    edgeMap[part]!!.findRelevantParts(typesInChild, parts)
                }
            }
        }

        fun printNode(indent: String = "") {
            println("$subtreeLevels")
            for (entry in edgeMap) {
                print("$indent ${entry.key} ")
                entry.value.printNode(indent + "  ")
            }
        }
    }
    
    companion object {
        val TYPES_TO_CONSIDER = 3L
    }
}