package ru.spb.se.contexthelper.lookup

import com.intellij.openapi.diagnostic.Logger
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.jsoup.Jsoup
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class GoogleSearchStackoverflowCrawler : QuestionLookupClient {
    private val randGenerator = Random(System.currentTimeMillis())
    private val cookies: String

    init {
        val cookieInputStream = javaClass.getResourceAsStream("/cookies.xml")
        val cookieDoc =
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(cookieInputStream)
        cookieDoc.documentElement.normalize()
        val cookieMap = mutableMapOf<String, String>()
        val cookieList = cookieDoc.getElementsByTagName("cookie")
        (0 until cookieList.length).forEach { i ->
            val cookieNode: Node = cookieList.item(i)
            if (cookieNode.nodeType == Node.ELEMENT_NODE) {
                val elem = cookieNode as Element
                val mMap = mutableMapOf<String, String>()
                (0 until elem.attributes.length).forEach { j ->
                    mMap.putIfAbsent(
                        elem.attributes.item(j).nodeName, elem.attributes.item(j).nodeValue)
                }
                val cookieName = mMap["name"]!!
                val cookieContent =
                    elem.getElementsByTagName("content").item(0).textContent
                cookieMap[cookieName] = cookieContent
            }
        }
        cookies = cookieMap.entries.joinToString("; ") { "${it.key}=${it.value}" }
    }

    override fun lookupQuestionIds(query: String): List<Long> {
        val resultIds = mutableListOf<Long>()
        try {
            var queryIndex = 0
            while (queryIndex < MAX_QUERIES) {
                val questionsIds =
                    lookupQuestionIdsStartingFrom(query, queryIndex * RESULTS_PER_PAGE)
                resultIds.addAll(questionsIds)
                queryIndex += 1
            }
        } catch (e: Exception) {
            LOG.error(e)
        }
        return resultIds.toList()
    }

    private fun lookupQuestionIdsStartingFrom(query: String, start: Int): List<Long> {
        Thread.sleep(WAIT_TIME_MIN_MS + randGenerator.nextInt(WAIT_TIME_SPREAD_MS))
        val uri = constructUri(query, start)
        LOG.info("crawler will issue HttpGet with $uri")
        val htmlContent = getPageContent(uri)
        val links = getLinksFromHtml(htmlContent)
        return getStackOverflowQuestionIds(links)
    }

    private fun constructUri(query: String, start: Int): URI {
        val stackoverflowQuery = "site:stackoverflow.com $query"
        val uriBuilder = URIBuilder()
            .setScheme("https")
            .setHost("www.google.com")
            .setPath("/search")
            .setParameter("q", stackoverflowQuery)
            .setParameter("hl", "en")
            .setParameter("gl", "en")
        if (start != 0) {
            uriBuilder.setParameter("start", "$start")
        }
        return uriBuilder.build()
    }


    private fun getPageContent(uri: URI): String {
        val request = HttpGet(uri)
        val agent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
        request.setHeader("User-Agent", agent)
        request.setHeader("Cookie", cookies)
        return HttpClients.createDefault().use { httpClient ->
            httpClient.execute(request).use { response ->
                exhaustInputStream(response.entity.content)
            }
        }
    }

    private fun exhaustInputStream(inputStream: InputStream): String {
        val builder = StringBuilder()
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                builder.append(line)
                line = reader.readLine()
            }
        }
        return builder.toString()
    }

    private fun getLinksFromHtml(content: String): List<String> {
        val links = mutableListOf<String>()
        val doc = Jsoup.parse(content)
        doc.select("h3").forEach { queryHeadElement ->
            queryHeadElement.select("a").forEach { linkElement ->
                var hrefValue = linkElement.attr("href")
                hrefValue = hrefValue.substring(hrefValue.indexOf("/url?q=") + 7)
                if (hrefValue.contains('&')) {
                    hrefValue = hrefValue.substring(0, hrefValue.indexOf('&'))
                }
                links.add(hrefValue)
            }
        }
        return links.toList()
    }

    private fun getStackOverflowQuestionIds(links: List<String>): List<Long> =
        links.mapNotNull {
            try {
                var uri = URI(it)
                var uriPathParts = uri.path.split('/')
                // Redirect reference links to /questions/id/...
                if (uriPathParts[1] != "questions") {
                    val getMethod = HttpGet(it)
                    HttpClients.createDefault().use { httpClient ->
                        val context = HttpClientContext.create()
                        httpClient.execute(getMethod, context).use {
                            uri = context.redirectLocations.last()
                            uriPathParts = uri.path.split('/')
                        }
                    }
                }
                if (uriPathParts[1] == "questions") {
                    // Question path: /questions/id/...
                    uriPathParts[2].toLong()
                } else {
                    LOG.warn("unknown path format for uri: $uri")
                    null
                }
            } catch (e: Exception) {
                LOG.warn(e.message)
                null
            }
        }.toList()

    companion object {
        private val LOG = Logger.getInstance(GoogleSearchStackoverflowCrawler::class.java)

        private const val MAX_QUERIES = 1
        private const val RESULTS_PER_PAGE = 10

        private const val WAIT_TIME_MIN_MS = 1 * 45 * 1000L
        private const val WAIT_TIME_SPREAD_MS = 1 * 45 * 1000
    }
}