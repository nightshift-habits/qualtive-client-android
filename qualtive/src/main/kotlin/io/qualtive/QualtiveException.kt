package io.qualtive

/**
 * Errors thrown by the Qualtive client.
 */
public sealed class QualtiveException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /** The requested enquiry (or related resource) was not found. */
    public class NotFound public constructor(
        message: String? = "Not found",
        cause: Throwable? = null,
    ) : QualtiveException(message, cause)

    /** A network connection error occurred. */
    public class Connection public constructor(
        message: String? = "Connection failed",
        cause: Throwable? = null,
    ) : QualtiveException(message, cause)

    /** The Qualtive API is temporarily unavailable for maintenance. */
    public class RemoteMaintenance public constructor(
        message: String? = "Remote maintenance",
        cause: Throwable? = null,
    ) : QualtiveException(message, cause)

    /** An unexpected error occurred. */
    public class Unexpected public constructor(
        message: String? = "Unexpected error",
        cause: Throwable? = null,
    ) : QualtiveException(message, cause)
}
