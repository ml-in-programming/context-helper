package ru.spb.se.contexthelper.lookup

import com.intellij.openapi.diagnostic.Logger
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection
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
            e.printStackTrace()
        }
        return resultIds.toList()
    }

    private fun lookupQuestionIdsStartingFrom(query: String, start: Int): List<Long> {
        Thread.sleep(WAIT_TIME_MIN_MS + randGenerator.nextInt(WAIT_TIME_SPREAD_MS))
        val stackoverflowQuery = "site:stackoverflow.com $query"
        val encodedQuery = URLEncoder.encode(stackoverflowQuery, "UTF-8")
        val urlQuery =
            "https://www.google.com/search?q=$encodedQuery&hl=en&gl=en" +
                if (start != 0) "&start=$start" else ""
        LOG.info("crawler issued urlQuery: $urlQuery")
        val pageContent = getSearchContent(urlQuery)
        val links = parseLinks(pageContent)
        return links.mapNotNull {
            // Format: https://stackoverflow.com/questions/id/...
            val urlParts = it.split(Regex("/"))
            try {
                val questionIdText = urlParts[4]
                questionIdText.toLong()
            } catch (e: Exception) {
                LOG.warn(e.message)
                null
            }
        }.toList()
    }

    private fun getSearchContent(urlQuery: String): String {
        val url = URL(urlQuery)
        val connection = url.openConnection() as HttpsURLConnection
        val agent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
        connection.setRequestProperty("User-Agent", agent)
        connection.setRequestProperty("Cookie", cookies)
        val responseInputStream = connection.inputStream
        return exhaustInputStream(responseInputStream)
    }

    private fun parseLinks(content: String): List<String> {
        val links = mutableListOf<String>()
        val pattern1 = "<h3 class=\"r\"><a href=\"/url?q="
        val pattern2 = "\">"
        val p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2))
        val matcher = p.matcher(content)
        while (matcher.find()) {
            var domainName = matcher.group(0).trim()
            domainName = domainName.substring(domainName.indexOf("/url?q=") + 7)
            domainName = domainName.substring(0, domainName.indexOf("&amp;"))
            links.add(domainName)
        }
        return links.toList()
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

    companion object {
        private val LOG =
            Logger.getInstance("ru.spb.se.contexthelper.lookup.GoogleSearchStackoverflowCrawler")

        private const val MAX_QUERIES = 1
        private const val RESULTS_PER_PAGE = 10

        private const val WAIT_TIME_MIN_MS = 15000L
        private const val WAIT_TIME_SPREAD_MS = 30000
    }
}