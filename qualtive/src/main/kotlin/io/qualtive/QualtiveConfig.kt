package io.qualtive

import java.util.Locale

/**
 * Construction-time configuration for a [Qualtive] client instance.
 *
 * Per-entry user identity, custom attributes, tracking consent, and metadata collection belong on
 * [Qualtive.post] / [PostOptions], not here.
 *
 * @param locale Locale used for localizable enquiry fields (`Accept-Language`). Defaults to the
 *   device locale.
 */
public class QualtiveConfig public constructor(
    public val locale: Locale = Locale.getDefault(),
)
