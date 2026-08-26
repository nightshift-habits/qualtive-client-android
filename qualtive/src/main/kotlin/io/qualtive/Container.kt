package io.qualtive

/** Container (workspace) details for an enquiry. */
public data class Container public constructor(
    public val id: String,
    public val isWhiteLabel: Boolean,
    public val logo: Logo?,
    public val customLogos: List<CustomLogo>,
    public val version: String,
    public val visibilityMode: VisibilityMode,
) {
    public data class Logo public constructor(
        public val urlVector: String?,
        public val urlVectorDark: String?,
    )

    public data class CustomLogo public constructor(
        public val size: Size,
        public val intendedBackground: IntendedBackground,
        public val primaryColor: String,
        public val urlVector: String,
    ) {
        public enum class Size {
            Wide,
            Square,
        }

        public enum class IntendedBackground {
            Light,
            Dark,
        }
    }

    public enum class VisibilityMode {
        Public,
        Private,
    }
}
