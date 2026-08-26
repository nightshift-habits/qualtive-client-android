package io.qualtive

/**
 * End-user identity attached to a single posted feedback entry.
 */
public data class User public constructor(
    public val id: String? = null,
    public val name: String? = null,
    public val email: String? = null,
)
