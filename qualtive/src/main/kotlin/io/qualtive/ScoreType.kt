package io.qualtive

/**
 * Kind of score control to display.
 */
public enum class ScoreType {
    Smilies5,
    Smilies3,
    Thumbs,
    Nps,
    Stars5,
    ;

    internal fun toApi(): String =
        when (this) {
            Smilies5 -> "smilies5"
            Smilies3 -> "smilies3"
            Thumbs -> "thumbs"
            Nps -> "nps"
            Stars5 -> "stars5"
        }

    internal companion object {
        fun fromApi(raw: String): ScoreType? =
            when (raw) {
                "smilies5" -> Smilies5
                "smilies3" -> Smilies3
                "thumbs" -> Thumbs
                "nps" -> Nps
                "stars5" -> Stars5
                else -> null
            }
    }
}
