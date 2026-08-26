package io.qualtive

import java.util.Locale

/**
 * Construction-time configuration for a [Qualtive] client instance.
 *
 * User identity, tracking consent, and custom attributes are runtime state on [Qualtive], not
 * here, so they can change after login without recreating the client.
 *
 * @param locale Locale used for localizable enquiry fields (`Accept-Language`). Defaults to the
 *   device locale.
 * @param metadataCollection Whether to attach device/app attributes when posting.
 */
public class QualtiveConfig public constructor(
    public val locale: Locale = Locale.getDefault(),
    public val metadataCollection: MetadataCollection = MetadataCollection.NonPersonal,
)
