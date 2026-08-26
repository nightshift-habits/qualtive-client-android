package io.qualtive

/** Thank-you / submitted page, optionally gated by score conditions. */
public data class SubmittedPage public constructor(
    public val content: List<Content>,
    public val conditions: List<Condition>,
) {

    /** Content block on a submitted page. */
    public sealed class Content {
        public data class Title public constructor(
            public val text: String,
        ) : Content()

        public data class Body public constructor(
            public val text: String,
        ) : Content()

        public data class Image public constructor(
            public val attachment: Attachment,
            public val linkUrl: String?,
        ) : Content()

        public data class ConfirmationText public constructor(
            public val text: String,
        ) : Content()

        public data object Name : Content()

        public data object UserInput : Content()

        public data object UserInputScore : Content()

        public data class Link public constructor(
            public val text: String,
            public val url: String,
        ) : Content()

        public data class ReviewLinks public constructor(
            public val links: List<Link>,
        ) : Content() {
            public data class Link public constructor(
                public val title: String,
                public val url: String,
                public val logo: Logo?,
                public val icon: Icon?,
            ) {
                public data class Logo public constructor(
                    public val urlVector: String,
                    public val urlVectorDark: String,
                )

                public data class Icon public constructor(
                    public val urlRaster: String,
                    public val urlRasterDark: String,
                )
            }
        }
    }

    /** Condition that must match for this submitted page to be shown. */
    public sealed class Condition {
        public data class Score public constructor(
            public val ranges: List<Range>,
        ) : Condition() {
            public data class Range public constructor(
                public val lower: Int?,
                public val upper: Int?,
            )
        }
    }
}
