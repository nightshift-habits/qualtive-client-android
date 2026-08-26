package io.qualtive

/**
 * A feedback entry (user response). Posting is implemented in a later ticket; this type holds the
 * content template produced by [Enquiry.entryContentTemplate].
 */
public data class Entry public constructor(
    public val id: Long? = null,
    public val content: List<Content> = emptyList(),
) {

    /** Content section for an entry, parallel to enquiry page content that accepts input. */
    public sealed class Content {
        public data class Title public constructor(
            public val text: String,
            public val definition: Page.Content.Title? = null,
        ) : Content()

        public data class Score public constructor(
            public val value: Int?,
            public val definition: Page.Content.Score? = null,
        ) : Content()

        public data class Text public constructor(
            public val value: String?,
            public val definition: Page.Content.Text? = null,
        ) : Content()

        public data class Select public constructor(
            public val value: String?,
            public val definition: Page.Content.Select? = null,
        ) : Content()

        public data class Multiselect public constructor(
            public val values: List<String>,
            public val definition: Page.Content.Multiselect? = null,
        ) : Content()

        public data class Attachments public constructor(
            public val attachments: List<AttachmentReference>,
            public val definition: Page.Content.Attachments? = null,
        ) : Content()
    }

    /** Reference to an uploaded attachment (id assigned after upload). */
    public data class AttachmentReference public constructor(
        public val id: Long,
    )
}
