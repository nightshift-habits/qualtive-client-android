package io.qualtive.internal.network

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class HttpUrlConnectionEngine(
    private val connectTimeoutMs: Int = 30_000,
    private val readTimeoutMs: Int = 30_000,
) : HttpEngine {
    override suspend fun execute(request: HttpRequest): HttpResponse =
        withContext(Dispatchers.IO) {
            val connection = (URL(request.url).openConnection() as HttpURLConnection).apply {
                requestMethod = request.method
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                doInput = true
                instanceFollowRedirects = true
                request.headers.forEach { (key, value) -> setRequestProperty(key, value) }
                if (request.body != null) {
                    doOutput = true
                    outputStream.use { it.write(request.body) }
                }
            }

            try {
                val statusCode =
                    try {
                        connection.responseCode
                    } catch (error: IOException) {
                        throw error
                    }

                val stream =
                    if (statusCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream ?: connection.inputStream
                    }

                val body = stream?.use { it.readBytes() } ?: ByteArray(0)
                HttpResponse(statusCode = statusCode, body = body)
            } finally {
                connection.disconnect()
            }
        }
}
