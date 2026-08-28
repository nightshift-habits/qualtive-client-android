package io.qualtive.internal

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import io.qualtive.AttachmentContentType
import io.qualtive.Enquiry
import io.qualtive.Entry
import io.qualtive.MetadataCollection
import io.qualtive.PostOptions
import io.qualtive.Qualtive
import io.qualtive.QualtiveConfig
import io.qualtive.QualtiveException
import io.qualtive.User
import io.qualtive.UserTrackingConsent
import io.qualtive.internal.enquiry.EnquiryParser
import io.qualtive.internal.entry.CreateAttachmentRequest
import io.qualtive.internal.entry.CreateAttachmentResponse
import io.qualtive.internal.entry.EntryPostEncoder
import io.qualtive.internal.entry.PostEntryResponse
import io.qualtive.internal.network.ApiClient
import io.qualtive.internal.network.HttpEngine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream
import java.net.URLEncoder
import java.util.TimeZone

internal class QualtiveImpl(
    override val containerId: String,
    override val workspaceId: String? = null,
    httpEngine: HttpEngine,
    internal val config: QualtiveConfig,
    context: Context? = null,
    baseUrl: String = DEFAULT_BASE_URL,
    clientIdStore: ClientIdStore? = null,
    deviceAttributesCollector: DeviceAttributesCollector? = null,
) : Qualtive {
    internal val applicationContext: Context? = context?.applicationContext

    private val api =
        ApiClient(
            httpEngine = httpEngine,
            baseUrl = baseUrl,
            containerId = containerId,
            workspaceId = workspaceId,
            config = config,
        )

    private val resolvedClientIdStore: ClientIdStore =
        clientIdStore
            ?: applicationContext?.let { SharedPreferencesClientIdStore(it) }
            ?: NoOpClientIdStore

    private val resolvedDeviceAttributesCollector: DeviceAttributesCollector =
        deviceAttributesCollector
            ?: applicationContext?.let { AndroidDeviceAttributesCollector(it) }
            ?: EmptyDeviceAttributesCollector

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

    override suspend fun post(
        enquiryId: String,
        content: List<Entry.Content>,
        user: User?,
        customAttributes: Map<String, Any>,
        options: PostOptions,
    ): Entry {
        require(enquiryId.isNotBlank()) { "enquiryId must not be blank" }

        val clientId =
            if (options.userTrackingConsent == UserTrackingConsent.Granted &&
                resolvedClientIdStore !== NoOpClientIdStore
            ) {
                resolvedClientIdStore.getOrCreate()
            } else {
                null
            }

        val deviceAttributes =
            if (options.metadataCollection == MetadataCollection.NonPersonal) {
                resolvedDeviceAttributesCollector.collect(config.locale)
            } else {
                emptyMap()
            }

        val mergedAttributes =
            EntryPostEncoder.mergeAttributes(
                device = deviceAttributes,
                custom = EntryPostEncoder.stringifyCustomAttributes(customAttributes),
                fromContent = EntryPostEncoder.attributesFromContent(content),
            )

        val body =
            EntryPostEncoder.encodeRequest(
                questionId = enquiryId,
                content = content,
                userId = user?.id,
                name = user?.name,
                email = user?.email,
                clientId = clientId,
                timeZoneIdentifier = TimeZone.getDefault().id,
                attributes = mergedAttributes,
            )

        val response =
            api.postJson(
                path = "/feedback/entries/",
                body = body,
                parse = { json.decodeFromString<PostEntryResponse>(it) },
            )

        return Entry(id = response.id, content = content)
    }

    override suspend fun uploadAttachment(
        uri: Uri,
        contentType: AttachmentContentType?,
    ): Entry.AttachmentReference {
        val context =
            applicationContext
                ?: throw QualtiveException.Unexpected(
                    "Context is required to upload from a Uri",
                )
        val resolver = context.contentResolver
        val resolvedType =
            contentType
                ?: resolver.getType(uri)?.takeIf { it.isNotBlank() }?.let(::AttachmentContentType)
                ?: throw QualtiveException.Unexpected(
                    "Could not determine content type for Uri; pass contentType explicitly",
                )
        val contentLength = contentLengthForUri(resolver, uri)
        return uploadAttachmentStreaming(
            contentType = resolvedType,
            contentLength = contentLength,
        ) { output ->
            resolver.openInputStream(uri)?.use { input ->
                input.copyTo(output)
            } ?: throw QualtiveException.Unexpected("Could not open Uri for reading")
        }
    }

    override suspend fun uploadAttachment(
        bytes: ByteArray,
        contentType: AttachmentContentType,
    ): Entry.AttachmentReference = uploadAttachmentStreaming(
        contentType = contentType,
        contentLength = bytes.size.toLong(),
    ) { output ->
        output.write(bytes)
    }

    private suspend fun uploadAttachmentStreaming(
        contentType: AttachmentContentType,
        contentLength: Long?,
        writeTo: (OutputStream) -> Unit,
    ): Entry.AttachmentReference {
        val createBody =
            json.encodeToString(CreateAttachmentRequest(contentType = contentType.mimeType))
                .toByteArray(Charsets.UTF_8)

        val created =
            api.postJson(
                path = "/feedback/attachments/",
                body = createBody,
                parse = { json.decodeFromString<CreateAttachmentResponse>(it) },
            )

        api.putAbsolute(
            url = created.uploadUrl,
            contentType = contentType.mimeType,
            contentLength = contentLength,
            writeTo = writeTo,
        )

        return Entry.AttachmentReference(id = created.id)
    }

    private fun contentLengthForUri(
        resolver: ContentResolver,
        uri: Uri,
    ): Long? {
        resolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
            val length = descriptor.length
            if (length >= 0L) {
                return length
            }
        }
        resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) {
                return@use
            }
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index < 0 || cursor.isNull(index)) {
                return@use
            }
            val size = cursor.getLong(index)
            if (size >= 0L) {
                return size
            }
        }
        return null
    }

    internal companion object {
        const val DEFAULT_BASE_URL: String = "https://user-api.qualtive.io/"

        private val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = false
                explicitNulls = false
            }

        private fun encodePathSegment(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
    }
}
