package io.qualtive.internal.network

internal data class HttpRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: ByteArray? = null,
)

internal data class HttpResponse(
    val statusCode: Int,
    val body: ByteArray,
)

internal interface HttpEngine {
    suspend fun execute(request: HttpRequest): HttpResponse
}
