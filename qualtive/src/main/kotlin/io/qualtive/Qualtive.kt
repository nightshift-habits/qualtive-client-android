package io.qualtive

import android.content.Context
import android.net.Uri
import io.qualtive.internal.QualtiveImpl
import io.qualtive.internal.network.HttpUrlConnectionEngine

/**
 * Qualtive client for a single container.
 *
 * Create an instance with [Qualtive] and inject it in your app (or use a fake in tests).
 *
 * Construction-time options belong on [QualtiveConfig]. User identity, custom attributes, and
 * privacy preferences are passed per [post] call.
 */
public interface Qualtive {
    /** Container (workspace) id this client talks to. */
    public val containerId: String

    /**
     * Fetches an enquiry definition.
     *
     * @param enquiryId Enquiry slug or numeric id as a string.
     * @param previewToken Optional preview token for unpublished enquiries.
     */
    public suspend fun fetchEnquiry(
        enquiryId: String,
        previewToken: String? = null,
    ): Enquiry

    /**
     * Posts a feedback entry for [enquiryId].
     *
     * Text sections with [Page.Content.Text.StorageTarget.Attribute] are sent as attributes, not
     * as text content.
     *
     * @param user Optional logged-in user for this entry.
     * @param customAttributes Optional custom attributes for this entry. Values must be [String],
     *   [Boolean], or a [Number].
     * @param options Per-post privacy / metadata options.
     */
    public suspend fun post(
        enquiryId: String,
        content: List<Entry.Content>,
        user: User? = null,
        customAttributes: Map<String, Any> = emptyMap(),
        options: PostOptions = PostOptions(),
    ): Entry

    /**
     * Uploads an attachment from a content [Uri] (photo, file, or video picker).
     *
     * Streams from the Uri into the upload request — the full file is not buffered in memory.
     * MIME type is read from [android.content.ContentResolver]. Pass [contentType] to override
     * when the resolver returns null.
     */
    public suspend fun uploadAttachment(
        uri: Uri,
        contentType: AttachmentContentType? = null,
    ): Entry.AttachmentReference

    /**
     * Uploads an attachment from bytes already in memory.
     *
     * Prefer the [Uri] overload for photos and especially video. Use this only for small payloads
     * you already hold as a [ByteArray].
     */
    public suspend fun uploadAttachment(
        bytes: ByteArray,
        contentType: AttachmentContentType,
    ): Entry.AttachmentReference
}

/**
 * Creates a Qualtive client for [containerId].
 *
 * [context] is retained as `applicationContext` for device/privacy features and Uri uploads.
 */
public fun Qualtive(
    context: Context,
    containerId: String,
    config: QualtiveConfig = QualtiveConfig(),
): Qualtive =
    QualtiveImpl(
        containerId = containerId,
        httpEngine = HttpUrlConnectionEngine(),
        config = config,
        context = context.applicationContext,
    )
