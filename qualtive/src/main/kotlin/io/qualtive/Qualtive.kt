package io.qualtive

import android.content.Context
import io.qualtive.internal.QualtiveImpl
import io.qualtive.internal.network.HttpUrlConnectionEngine

/**
 * Qualtive client for a single container.
 *
 * Create an instance with [Qualtive] and inject it in your app (or use a fake in tests).
 *
 * Construction-time options belong on [QualtiveConfig]. User identity, tracking consent, and
 * custom attributes are runtime state on this client so they can change after login.
 */
public interface Qualtive {
    /** Container (workspace) id this client talks to. */
    public val containerId: String

    /**
     * Whether a persisted client id may be stored. Defaults to [UserTrackingConsent.Denied].
     *
     * Independent of [QualtiveConfig.metadataCollection].
     */
    public var userTrackingConsent: UserTrackingConsent

    /**
     * Fetches an enquiry definition.
     *
     * @param enquiryId Enquiry slug or numeric id as a string.
     * @param previewToken Optional preview token for unpublished enquiries.
     */
    public suspend fun fetchEnquiry(
        enquiryId: String,
        previewToken: String? = null,
    ): Enquiry

    /** Associates the current user with later posts. Pass nulls to clear. */
    public fun identify(
        userId: String? = null,
        name: String? = null,
        email: String? = null,
    )

    /** Sets a custom string attribute sent with later posts. */
    public fun setAttribute(
        name: String,
        value: String,
    )

    /** Sets a custom boolean attribute sent with later posts. */
    public fun setAttribute(
        name: String,
        value: Boolean,
    )

    /** Sets a custom numeric attribute sent with later posts. */
    public fun setAttribute(
        name: String,
        value: Int,
    )

    /** Sets a custom numeric attribute sent with later posts. */
    public fun setAttribute(
        name: String,
        value: Long,
    )

    /** Sets a custom numeric attribute sent with later posts. */
    public fun setAttribute(
        name: String,
        value: Double,
    )

    /** Removes a previously set custom attribute. */
    public fun removeAttribute(name: String)
}

/**
 * Creates a Qualtive client for [containerId].
 *
 * [context] is retained as `applicationContext` for device/privacy features.
 */
public fun Qualtive(
    context: Context,
    containerId: String,
    config: QualtiveConfig = QualtiveConfig(),
): Qualtive =
    QualtiveImpl(
        containerId = containerId,
        httpEngine = HttpUrlConnectionEngine(),
        config = config,
        context = context.applicationContext,
    )
