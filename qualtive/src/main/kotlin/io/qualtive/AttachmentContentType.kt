package io.qualtive

/**
 * MIME type for an uploaded attachment.
 *
 * Any MIME string is accepted (for example `application/pdf`, `video/mp4`, `image/jpeg`).
 */
@JvmInline
public value class AttachmentContentType public constructor(
    public val mimeType: String,
) {
    init {
        require(mimeType.isNotBlank()) { "contentType must not be blank" }
    }

    public companion object {
        /** Convenience for `image/jpeg`. */
        public val ImageJpeg: AttachmentContentType = AttachmentContentType("image/jpeg")

        /** Convenience for `image/png`. */
        public val ImagePng: AttachmentContentType = AttachmentContentType("image/png")
    }
}
