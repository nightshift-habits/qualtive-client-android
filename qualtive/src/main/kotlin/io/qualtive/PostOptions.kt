package io.qualtive

/**
 * Per-post options for [Qualtive.post].
 *
 * @param metadataCollection Whether to attach device/app attributes. Defaults to
 *   [MetadataCollection.NonPersonal].
 * @param userTrackingConsent Whether a persisted client id may be stored/sent. Defaults to
 *   [UserTrackingConsent.Granted].
 */
public data class PostOptions public constructor(
    public val metadataCollection: MetadataCollection = MetadataCollection.NonPersonal,
    public val userTrackingConsent: UserTrackingConsent = UserTrackingConsent.Granted,
)
