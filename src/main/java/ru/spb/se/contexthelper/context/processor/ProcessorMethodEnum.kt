package ru.spb.se.contexthelper.context.processor

/** Represents a enum of available ContextProcessors. */
enum class ProcessorMethodEnum(val methodName: String) {
    GoogleSearchMethod("GoogleSearchContext Method"),
    GoogleSearchNaiveMethod("GoogleSearchNaive method"),
    TypeNodeIndexMethod("TypeNodeIndex Method"),
}