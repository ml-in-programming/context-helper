package ru.spb.se.contexthelper.errors

import java.util.ArrayList

class ErrorMessage {
    private val error: MutableList<String>
    private val warning: MutableList<String>
    private val information: MutableList<String>

    init {
        error = ArrayList()
        warning = ArrayList()
        information = ArrayList()
    }

    fun get(messageType: MessageType): Array<String> {
        return when (messageType) {
            ErrorMessage.MessageType.ERROR -> error.toTypedArray()
            ErrorMessage.MessageType.WARNING -> warning.toTypedArray()
            ErrorMessage.MessageType.INFORMATION -> information.toTypedArray()
        }
    }

    fun put(messageType: MessageType, text: String) {
        when (messageType) {
            ErrorMessage.MessageType.ERROR -> error.add(text)
            ErrorMessage.MessageType.WARNING -> warning.add(text)
            ErrorMessage.MessageType.INFORMATION -> information.add(text)
        }
    }

    fun put(messageType: MessageType, texts: List<String>) {
        texts.forEach { t -> put(messageType, t) }
    }

    enum class MessageType {
        ERROR,
        WARNING,
        INFORMATION
    }
}