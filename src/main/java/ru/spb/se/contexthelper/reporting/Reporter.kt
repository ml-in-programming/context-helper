package ru.spb.se.contexthelper.reporting

import com.google.common.net.HttpHeaders
import com.google.gson.Gson
import com.intellij.openapi.application.PermanentInstallationID
import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.codec.binary.Base64OutputStream
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.apache.http.message.BasicHeader
import ru.spb.se.contexthelper.log.Log
import ru.spb.se.contexthelper.log.data.LogData
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

private class StatsServerInfo(@JvmField var status: String,
                              @JvmField var url: String,
                              @JvmField var urlForZipBase64Content: String) {
    fun isServiceAlive() = "ok" == status
}

private object Utils {
    val gson = Gson()
}

object StatsSender {
    private val infoUrl = "https://www.jetbrains.com/config/features-service-status.json"
    private val LOG = Logger.getInstance(StatsSender::class.java)

    private fun requestServerUrl(): StatsServerInfo? {
        try {
            val response = Request.Get(infoUrl).execute().returnContent().asString()
            val info = Utils.gson.fromJson(response, StatsServerInfo::class.java)
            if (info.isServiceAlive()) return info
        }
        catch (e: Exception) {
            LOG.debug(e)
        }

        return null
    }

    fun send(text: String, compress: Boolean = true): Boolean {
        val info = requestServerUrl() ?: return false
        try {
            val response = createRequest(info, text, compress).execute()
            val code = response.handleResponse { it.statusLine.statusCode }
            if (code in 200..299) {
                return true
            }
        }
        catch (e: Exception) {
            LOG.debug(e)
        }
        return false
    }

    private fun createRequest(info: StatsServerInfo, text: String, compress: Boolean): Request {
        if (compress) {
            val data = Base64GzipCompressor.compress(text)
            val request = Request.Post(info.urlForZipBase64Content).bodyByteArray(data)
            request.addHeader(BasicHeader(HttpHeaders.CONTENT_ENCODING, "gzip"))
            return request
        }

        return Request.Post(info.url).bodyString(text, ContentType.TEXT_HTML)
    }

}

private object Base64GzipCompressor {
    fun compress(text: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val base64Stream = GZIPOutputStream(Base64OutputStream(outputStream))
        base64Stream.write(text.toByteArray())
        base64Stream.close()
        return outputStream.toByteArray()
    }
}

fun createLogLine(sessionId: String, actionType: String, logData: LogData): String {
    val stamp = System.currentTimeMillis()
    val recorderId = "context-helper"
    val recorderVersion = "1.0"
    val userId = PermanentInstallationID.get()
    val log = Log(stamp, recorderId, recorderVersion, userId, sessionId, actionType, logData)
    return log.toString()
}