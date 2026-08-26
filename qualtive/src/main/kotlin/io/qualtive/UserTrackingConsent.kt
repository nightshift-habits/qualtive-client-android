package io.qualtive

/**
 * Whether the current user has consented to a persisted client id.
 *
 * [Denied] means no client id is stored. Device/app metadata is controlled separately by
 * [MetadataCollection].
 */
public enum class UserTrackingConsent {
    Granted,
    Denied,
}
