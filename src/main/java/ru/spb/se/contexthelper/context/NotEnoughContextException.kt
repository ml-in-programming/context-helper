package ru.spb.se.contexthelper.context

/** Exception is thrown if the query can not be built because of the lack of the context. */
class NotEnoughContextException(message: String) : RuntimeException(message)