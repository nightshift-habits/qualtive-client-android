package io.qualtive.internal.network

import java.io.OutputStream

internal data class HttpRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: HttpRequestBody? = null,
    val followRedirects: Boolean = true,
)

internal sealed class HttpRequestBody {
    class Bytes(
        val bytes: ByteArray,
    ) : HttpRequestBody()

    /**
     * Streams request body without buffering the full payload in memory.
     *
     * When [contentLength] is known, fixed-length streaming is used. Otherwise the engine falls
     * back to chunked transfer encoding.
     */
    class Streaming(
        val contentLength: Long?,
        val writeTo: (OutputStream) -> Unit,
    ) : HttpRequestBody()
}

internal data class HttpResponse(
    val statusCode: Int,
    val body: ByteArray,
)

internal interface HttpEngine {
    suspend fun execute(request: HttpRequest): HttpResponse
}
