package io.qualtive

/** A single page of enquiry content. */
public data class Page public constructor(
    public val content: List<Content>,
) {

    /** Content block shown on an enquiry page. */
    public sealed class Content {
        /** Static title. */
        public data class Title public constructor(
            public val text: String,
        ) : Content()

        /** Static body text. */
        public data class Body public constructor(
            public val text: String,
        ) : Content()

        /** Static image. */
        public data class Image public constructor(
            public val attachment: Attachment,
        ) : Content()

        /** Score / rating input (0–100). */
        public data class Score public constructor(
            public val scoreType: ScoreType,
            public val leadingText: String?,
            public val trailingText: String?,
        ) : Content()

        /** Free-form text input. */
        public data class Text public constructor(
            public val placeholder: String?,
            public val storageTarget: StorageTarget,
        ) : Content() {
            /** Where text input is stored when posting. */
            public sealed class StorageTarget {
                /** Stored as entry text content. */
                public data object Text : StorageTarget()

                /** Stored as a custom attribute. */
                public data class Attribute public constructor(
                    public val attribute: String,
                ) : StorageTarget()
            }
        }

        /** Single-select input. */
        public data class Select public constructor(
            public val options: List<String>,
            public val allowsCustomInput: Boolean,
        ) : Content()

        /** Multi-select input. */
        public data class Multiselect public constructor(
            public val options: List<String>,
        ) : Content()

        /** Attachments input. */
        public data object Attachments : Content()

        /** Contact details input. */
        public data class ContactDetails public constructor(
            public val title: String,
            public val placeholder: String?,
        ) : Content()
    }
}
