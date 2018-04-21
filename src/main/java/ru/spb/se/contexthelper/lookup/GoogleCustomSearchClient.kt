package ru.spb.se.contexthelper.lookup

import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.customsearch.Customsearch
import com.intellij.openapi.diagnostic.Logger

class GoogleCustomSearchClient(private val apiKey: String) : QuestionLookupClient {
    override fun lookupQuestionIds(query: String): List<Long> {
        val builder = Customsearch.Builder(
            NetHttpTransport(),
            JacksonFactory(),
            HttpRequestInitializer { request ->
                request.connectTimeout = HTTP_REQUEST_TIMEOUT
                request.readTimeout = HTTP_READ_TIMEOUT
            }
        )
        builder.applicationName = "SnippetSearch"
        val customsearch = builder.build()
        val list = customsearch.cse().list(query)
        list.key = apiKey
        list.cx = SEARCH_ENGINE_ID
        list.alt = "json"
        val searchResult = list.execute()
        searchResult.items ?: return emptyList()
        return searchResult.items.mapNotNull {
            val link = it.link
            // Format: https://stackoverflow.com/questions/id/...
            val urlParts = link.split(Regex("/"))
            try {
                val questionIdText = urlParts[4]
                questionIdText.toLong()
            } catch (e: Exception) {
                LOG.warn(e.message)
                null
            }
        }.toList()
    }

    companion object {
        private val LOG = Logger.getInstance(GoogleCustomSearchClient::class.java)

        private const val SEARCH_ENGINE_ID = "004273159360178116673:j1srnoyrr-i"

        private const val HTTP_REQUEST_TIMEOUT = 5000
        private const val HTTP_READ_TIMEOUT = 5000
    }
}