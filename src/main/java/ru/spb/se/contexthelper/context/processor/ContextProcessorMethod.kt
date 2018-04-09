package ru.spb.se.contexthelper.context.processor

/** Represents a enum of available ContextProcessors. */
enum class ContextProcessorMethod(val methodName: String) {
    GCSMethod("Google Custom Search method"),
    TypeNodeIndexMethod("TypeNode Index method")
}