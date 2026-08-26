package io.qualtive.internal

import android.content.Context
import io.qualtive.Enquiry
import io.qualtive.Qualtive
import io.qualtive.QualtiveConfig
import io.qualtive.UserTrackingConsent
import io.qualtive.internal.enquiry.EnquiryParser
import io.qualtive.internal.network.ApiClient
import io.qualtive.internal.network.HttpEngine
import java.net.URLEncoder

internal class QualtiveImpl(
    override val containerId: String,
    httpEngine: HttpEngine,
    internal val config: QualtiveConfig,
    context: Context? = null,
    baseUrl: String = DEFAULT_BASE_URL,
) : Qualtive {
    // Retained for QUAL-575 (persisted client id and device attributes).
    internal val applicationContext: Context? = context?.applicationContext

    private val api =
        ApiClient(
            httpEngine = httpEngine,
            baseUrl = baseUrl,
            containerId = containerId,
            config = config,
        )

    private val stateLock = Any()
    private var identifiedUserId: String? = null
    private var identifiedName: String? = null
    private var identifiedEmail: String? = null
    private val attributes = linkedMapOf<String, AttributeValue>()

    override var userTrackingConsent: UserTrackingConsent = UserTrackingConsent.Denied

    override suspend fun fetchEnquiry(
        enquiryId: String,
        previewToken: String?,
    ): Enquiry {
        require(enquiryId.isNotBlank()) { "enquiryId must not be blank" }

        val query =
            if (previewToken.isNullOrBlank()) {
                emptyMap()
            } else {
                mapOf("previewToken" to previewToken)
            }

        return api.get(
            path = "/feedback/enquiries/${encodePathSegment(enquiryId)}/",
            query = query,
            parse = EnquiryParser::parse,
        )
    }

    override fun identify(
        userId: String?,
        name: String?,
        email: String?,
    ) {
        synchronized(stateLock) {
            identifiedUserId = userId
            identifiedName = name
            identifiedEmail = email
        }
    }

    override fun setAttribute(
        name: String,
        value: String,
    ) {
        putAttribute(name, AttributeValue.Text(value))
    }

    override fun setAttribute(
        name: String,
        value: Boolean,
    ) {
        putAttribute(name, AttributeValue.Flag(value))
    }

    override fun setAttribute(
        name: String,
        value: Int,
    ) {
        putAttribute(name, AttributeValue.Number(value.toDouble()))
    }

    override fun setAttribute(
        name: String,
        value: Long,
    ) {
        putAttribute(name, AttributeValue.Number(value.toDouble()))
    }

    override fun setAttribute(
        name: String,
        value: Double,
    ) {
        putAttribute(name, AttributeValue.Number(value))
    }

    override fun removeAttribute(name: String) {
        synchronized(stateLock) {
            attributes.remove(name)
        }
    }

    internal fun snapshotIdentity(): IdentifiedUser =
        synchronized(stateLock) {
            IdentifiedUser(
                userId = identifiedUserId,
                name = identifiedName,
                email = identifiedEmail,
            )
        }

    internal fun snapshotAttributes(): Map<String, AttributeValue> =
        synchronized(stateLock) {
            attributes.toMap()
        }

    private fun putAttribute(
        name: String,
        value: AttributeValue,
    ) {
        require(name.isNotBlank()) { "attribute name must not be blank" }
        synchronized(stateLock) {
            attributes[name] = value
        }
    }

    internal companion object {
        const val DEFAULT_BASE_URL: String = "https://user-api.qualtive.io/"

        private fun encodePathSegment(value: String): String =
            URLEncoder.encode(value, "UTF-8").replace("+", "%20")
    }
}

internal data class IdentifiedUser(
    val userId: String?,
    val name: String?,
    val email: String?,
)

internal sealed class AttributeValue {
    data class Text(
        val value: String,
    ) : AttributeValue()

    data class Number(
        val value: Double,
    ) : AttributeValue()

    data class Flag(
        val value: Boolean,
    ) : AttributeValue()
}
