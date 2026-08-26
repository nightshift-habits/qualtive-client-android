package io.qualtive

/**
 * How much non-user metadata may be attached for a single [Qualtive.post].
 *
 * Passed via [PostOptions.metadataCollection]. Independent of [UserTrackingConsent], which only
 * controls a persisted client id.
 */
public enum class MetadataCollection {
    /** Device and app attributes (for example `clientLibrary: android`). */
    NonPersonal,

    /** No automatic metadata. */
    None,
}
