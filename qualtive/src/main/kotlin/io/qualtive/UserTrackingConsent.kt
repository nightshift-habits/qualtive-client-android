package io.qualtive

/**
 * Whether the user has consented to a persisted client id for a single [Qualtive.post].
 *
 * Passed via [PostOptions.userTrackingConsent]. [Denied] means no client id is stored or sent.
 * Device/app metadata is controlled separately by [MetadataCollection].
 */
public enum class UserTrackingConsent {
    Granted,
    Denied,
}
