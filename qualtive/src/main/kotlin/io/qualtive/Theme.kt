package io.qualtive

/** Visual theme for an enquiry. */
public data class Theme public constructor(
    public val background: Background,
    public val font: Font,
    public val cornerStyle: CornerStyle,
    public val isBackgroundAttachmentVisibleInResponses: Boolean,
    public val isBackgroundColorVisibleInResponses: Boolean,
) {
    public sealed class Background {
        public data class Predefined public constructor(
            public val value: Value,
        ) : Background() {
            public enum class Value {
                Plain,
                Sponda,
            }
        }

        public data class Custom public constructor(
            public val attachment: Attachment?,
            public val color: Color,
        ) : Background() {
            public data class Attachment public constructor(
                public val id: Long,
                public val contentType: String,
                public val url: String,
            )

            public data class Color public constructor(
                public val value: String,
            )
        }
    }

    public sealed class Font {
        public data class Predefined public constructor(
            public val value: String,
        ) : Font()

        public data class Custom public constructor(
            public val url: String,
        ) : Font()
    }

    public enum class CornerStyle {
        Rounded,
        Square,
    }
}
