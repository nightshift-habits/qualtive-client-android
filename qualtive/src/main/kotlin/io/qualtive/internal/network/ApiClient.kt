package io.qualtive.internal.network

import io.qualtive.QualtiveConfig
import io.qualtive.QualtiveException
import java.io.IOException
import java.net.URLEncoder

internal class ApiClient(
    private val httpEngine: HttpEngine,
    private val baseUrl: String,
    private val containerId: String,
    private val config: QualtiveConfig,
) {
    suspend fun <T> get(
        path: String,
        query: Map<String, String> = emptyMap(),
        parse: (String) -> T,
    ): T {
        val url = buildUrl(path, query)
        val response =
            try {
                httpEngine.execute(
                    HttpRequest(
                        method = "GET",
                        url = url,
                        headers =
                            mapOf(
                                "X-Container" to containerId,
                                "Accept-Language" to config.locale.toLanguageTag(),
                                "Accept" to "application/json",
                            ),
                    ),
                )
            } catch (error: IOException) {
                throw QualtiveException.Connection(cause = error)
            } catch (error: QualtiveException) {
                throw error
            } catch (error: Exception) {
                throw QualtiveException.Unexpected(cause = error)
            }

        val body = response.body.toString(Charsets.UTF_8)
        return when (response.statusCode) {
            in 200..299 ->
                try {
                    parse(body)
                } catch (error: QualtiveException) {
                    throw error
                } catch (error: Exception) {
                    throw QualtiveException.Unexpected("Failed to parse response", error)
                }
            404 -> throw QualtiveException.NotFound()
            503 -> throw QualtiveException.RemoteMaintenance()
            else ->
                throw QualtiveException.Unexpected("Unexpected status ${response.statusCode}")
        }
    }

    private fun buildUrl(
        path: String,
        query: Map<String, String>,
    ): String {
        val normalizedPath =
            if (path.startsWith("/")) {
                path
            } else {
                "/$path"
            }
        val queryString =
            if (query.isEmpty()) {
                ""
            } else {
                "?" +
                    query.entries.joinToString("&") { (key, value) ->
                        "${encode(key)}=${encode(value)}"
                    }
            }
        return baseUrl.trimEnd('/') + normalizedPath + queryString
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
