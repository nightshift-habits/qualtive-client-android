package io.qualtive.internal.network

import java.io.ByteArrayOutputStream

internal class FakeHttpEngine(
    private val handler: (HttpRequest) -> HttpResponse,
) : HttpEngine {
    val requests: MutableList<HttpRequest> = mutableListOf()

    override suspend fun execute(request: HttpRequest): HttpResponse {
        requests += request
        return handler(request)
    }
}

internal fun HttpRequestBody?.readAllBytes(): ByteArray? =
    when (this) {
        null -> null
        is HttpRequestBody.Bytes -> bytes
        is HttpRequestBody.Streaming -> {
            val buffer = ByteArrayOutputStream()
            writeTo(buffer)
            buffer.toByteArray()
        }
    }
