package ru.spb.se.contexthelper.lookup

import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.customsearch.Customsearch
import com.intellij.openapi.diagnostic.Logger

class StackOverflowGoogleSearchClient(private val apiKey: String) {
    fun lookupQuestionIds(query: String): List<Long> {
        val customsearch =
            Customsearch(
                NetHttpTransport(),
                JacksonFactory(),
                HttpRequestInitializer { request ->
                    request.connectTimeout = HTTP_REQUEST_TIMEOUT
                    request.readTimeout = HTTP_READ_TIMEOUT
                }
            )
        val list = customsearch.cse().list("$query java")
        list.key = apiKey
        list.cx = SEARCH_ENGINE_ID
        list.alt = "json"
        val searchResult = list.execute()
        return searchResult.items.mapNotNull {
            val link = it.link
            // Format: https://stackoverflow.com/questions/id/...
            val urlParts = link.split(Regex("/"))
            val questionIdText = urlParts[4]
            try {
                questionIdText.toLong()
            } catch (e: NumberFormatException) {
                LOG.warn(e.message)
                null
            }
        }.toList()
    }

    companion object {
        private val LOG = Logger.getInstance(
            "ru.spb.se.contexthelper.lookup.StackOverflowGoogleSearchClient")

        private var SEARCH_ENGINE_ID = "004273159360178116673:j1srnoyrr-i"

        private val HTTP_REQUEST_TIMEOUT = 5000
        private val HTTP_READ_TIMEOUT = 5000
    }
}