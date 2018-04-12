package ru.spb.se.contexthelper.context.processor

/** Represents a enum of available ContextProcessors. */
enum class ContextProcessorMethod(val methodName: String) {
    GoogleSearchMethod("Google Search method"),
    TypeNodeIndexMethod("TypeNode Index method")
}