package io.qualtive

/**
 * How much non-user metadata the client may attach when posting.
 *
 * Independent of [UserTrackingConsent], which only controls a persisted client id.
 */
public enum class MetadataCollection {
    /** Device and app attributes (for example `clientLibrary: android`). */
    NonPersonal,

    /** No automatic metadata. */
    None,
}
